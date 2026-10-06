"""Storage behind an interface; swap JsonSeedStore for a database-backed class."""
import json
from pathlib import Path
from typing import Protocol

SEED = Path(__file__).parent / "mock" / "reminders.json"


class ReminderStore(Protocol):
    def all(self) -> list[dict]: ...
    def get(self, rid: int) -> dict | None: ...
    def add(self, data: dict) -> dict: ...
    def update(self, rid: int, changes: dict) -> dict | None: ...
    def delete(self, rid: int) -> bool: ...


class JsonSeedStore:
    """In-memory store seeded from mock/reminders.json."""

    def __init__(self, path: Path = SEED):
        self._items = {r["id"]: r for r in json.loads(path.read_text())}
        self._next = max(self._items, default=0) + 1

    def all(self) -> list[dict]:
        return sorted(self._items.values(), key=lambda r: r["due"])

    def get(self, rid: int) -> dict | None:
        return self._items.get(rid)

    def add(self, data: dict) -> dict:
        item = {"id": self._next, "done": False, **data}
        self._items[self._next] = item
        self._next += 1
        return item

    def update(self, rid: int, changes: dict) -> dict | None:
        item = self._items.get(rid)
        if item is not None:
            item.update(changes)
        return item

    def delete(self, rid: int) -> bool:
        return self._items.pop(rid, None) is not None
