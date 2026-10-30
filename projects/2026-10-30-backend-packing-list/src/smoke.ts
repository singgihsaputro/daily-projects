import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import { buildApp } from "./app.js";
import { JsonPackingStore } from "./store.js";

const mock = (name: string) => fileURLToPath(new URL(`../mock/${name}`, import.meta.url));
const app = buildApp(await JsonPackingStore.load(mock("templates.json"), mock("trips.json")));
const send = (path: string, method = "GET", body?: unknown) =>
  app.request(path, { method, headers: { "Content-Type": "application/json" }, body: body ? JSON.stringify(body) : undefined });

let res = await send("/api/trips", "POST", { name: "Alps", days: 10, template: "hiking" });
assert.equal(res.status, 201);
const trip = await res.json();
const socks = trip.items.find((i: { name: string }) => i.name === "Hiking socks");
assert.equal(socks.qty, 5, "per-day quantities are capped by max");

assert.equal((await send("/api/trips", "POST", { name: "x", days: 0, template: "hiking" })).status, 400);
assert.equal((await send("/api/trips", "POST", { name: "x", days: 2, template: "moon" })).status, 400);

res = await send(`/api/trips/${trip.id}/items/${socks.id}`, "PATCH", { packed: true });
assert.equal((await res.json()).packed, true);
res = await send(`/api/trips/${trip.id}`);
assert.equal((await res.json()).progress.packed, 1);

assert.equal((await send(`/api/trips/${trip.id}/items/nope`, "DELETE")).status, 404);
assert.equal((await send(`/api/trips/${trip.id}/items/${socks.id}`, "DELETE")).status, 204);
assert.equal((await send("/api/trips/missing")).status, 404);
console.log("all smoke checks passed");
