"""Runs the API in-process and checks every endpoint: python smoke_test.py"""
from fastapi.testclient import TestClient

from main import app

c = TestClient(app)
NOW = "2026-10-06T06:00:00Z"

r = c.get("/reminders", params={"now": NOW})
assert r.status_code == 200 and len(r.json()) == 5
assert [x["id"] for x in c.get("/reminders", params={"status": "overdue", "now": NOW}).json()] == [3]
assert [x["id"] for x in c.get("/reminders/due", params={"hours": 24, "now": NOW}).json()] == [2]
assert c.get("/reminders", params={"status": "bogus"}).status_code == 422

r = c.post("/reminders", json={"title": "  Call mum ", "due": "2026-10-07T18:00:00+07:00", "priority": "high"})
assert r.status_code == 201 and r.json()["title"] == "Call mum" and r.json()["due"] == "2026-10-07T11:00:00Z"
rid = r.json()["id"]
assert r.headers["location"] == f"/reminders/{rid}"
assert c.post("/reminders", json={"title": "", "due": NOW}).status_code == 422

assert c.patch(f"/reminders/{rid}", json={"priority": "low"}).json()["priority"] == "low"
assert c.post(f"/reminders/{rid}/complete").json()["status"] == "done"
assert c.delete(f"/reminders/{rid}").status_code == 204
assert c.get(f"/reminders/{rid}").status_code == 404
assert c.delete(f"/reminders/{rid}").status_code == 404
print("all checks passed")
