from datetime import datetime, timezone
from enum import Enum

from fastapi import Depends, FastAPI, HTTPException, Query, Response
from pydantic import BaseModel, Field

from store import JsonSeedStore, ReminderStore

app = FastAPI(title="Reminder API", version="1.0.0")
_store = JsonSeedStore()


def get_store() -> ReminderStore:
    return _store


class Priority(str, Enum):
    low = "low"
    normal = "normal"
    high = "high"


class ReminderIn(BaseModel):
    title: str = Field(min_length=1, max_length=120)
    due: datetime
    priority: Priority = Priority.normal


class ReminderPatch(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=120)
    due: datetime | None = None
    priority: Priority | None = None


def _utc(dt: datetime) -> datetime:
    return dt.replace(tzinfo=timezone.utc) if dt.tzinfo is None else dt.astimezone(timezone.utc)


def _iso(dt: datetime) -> str:
    return _utc(dt).strftime("%Y-%m-%dT%H:%M:%SZ")


def _parse(s: str) -> datetime:
    return datetime.fromisoformat(s.replace("Z", "+00:00"))


def _view(item: dict, now: datetime) -> dict:
    overdue = not item["done"] and _parse(item["due"]) < now
    status = "done" if item["done"] else "overdue" if overdue else "upcoming"
    return {**item, "status": status}


def _now(now: datetime | None) -> datetime:
    return _utc(now) if now else datetime.now(timezone.utc)


def _or_404(store: ReminderStore, rid: int) -> dict:
    item = store.get(rid)
    if item is None:
        raise HTTPException(404, f"reminder {rid} not found")
    return item


@app.get("/reminders")
def list_reminders(status: str | None = Query(None, pattern="^(upcoming|overdue|done)$"),
                   now: datetime | None = None, store: ReminderStore = Depends(get_store)):
    t = _now(now)
    views = [_view(r, t) for r in store.all()]
    return [v for v in views if status is None or v["status"] == status]


@app.get("/reminders/due")
def due_within(hours: int = Query(24, ge=1, le=720), now: datetime | None = None,
               store: ReminderStore = Depends(get_store)):
    """Open reminders due between now and now + hours (overdue ones are excluded)."""
    t = _now(now)
    out = []
    for r in store.all():
        delta = (_parse(r["due"]) - t).total_seconds() / 3600
        if not r["done"] and 0 <= delta <= hours:
            out.append({**_view(r, t), "hours_left": round(delta, 1)})
    return out


@app.post("/reminders", status_code=201)
def create(body: ReminderIn, response: Response, store: ReminderStore = Depends(get_store)):
    item = store.add({"title": body.title.strip(), "due": _iso(body.due), "priority": body.priority.value})
    response.headers["Location"] = f"/reminders/{item['id']}"
    return _view(item, datetime.now(timezone.utc))


@app.get("/reminders/{rid}")
def read(rid: int, store: ReminderStore = Depends(get_store)):
    return _view(_or_404(store, rid), datetime.now(timezone.utc))


@app.patch("/reminders/{rid}")
def patch(rid: int, body: ReminderPatch, store: ReminderStore = Depends(get_store)):
    _or_404(store, rid)
    changes = body.model_dump(exclude_none=True)
    if "due" in changes:
        changes["due"] = _iso(changes["due"])
    if "priority" in changes:
        changes["priority"] = changes["priority"].value
    return _view(store.update(rid, changes), datetime.now(timezone.utc))


@app.post("/reminders/{rid}/complete")
def complete(rid: int, store: ReminderStore = Depends(get_store)):
    _or_404(store, rid)
    return _view(store.update(rid, {"done": True}), datetime.now(timezone.utc))


@app.delete("/reminders/{rid}", status_code=204)
def remove(rid: int, store: ReminderStore = Depends(get_store)):
    if not store.delete(rid):
        raise HTTPException(404, f"reminder {rid} not found")
