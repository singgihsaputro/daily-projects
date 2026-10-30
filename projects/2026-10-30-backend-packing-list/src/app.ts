import { Hono } from "hono";
import { NotFoundError, type PackingStore } from "./store.js";
import type { Trip } from "./types.js";

function summarize(trip: Trip) {
  const packed = trip.items.filter((i) => i.packed).length;
  const byCategory: Record<string, typeof trip.items> = {};
  for (const item of trip.items) (byCategory[item.category] ??= []).push(item);
  return {
    ...trip,
    progress: { packed, total: trip.items.length, percent: trip.items.length ? Math.round((packed / trip.items.length) * 100) : 0 },
    byCategory,
  };
}

export function buildApp(store: PackingStore): Hono {
  const app = new Hono();

  app.onError((err, c) => {
    if (err instanceof NotFoundError) return c.json({ error: err.message }, 404);
    return c.json({ error: "internal error" }, 500);
  });

  app.get("/api/templates", (c) => c.json(store.listTemplates().map(({ id, label }) => ({ id, label }))));

  app.get("/api/trips", (c) =>
    c.json(store.listTrips().map((t) => {
      const { id, name, days, template, progress } = summarize(t);
      return { id, name, days, template, progress };
    })),
  );

  app.post("/api/trips", async (c) => {
    const body = await c.req.json().catch(() => null);
    if (!body || typeof body.name !== "string" || !body.name.trim()) {
      return c.json({ error: "\"name\" is required" }, 400);
    }
    if (!Number.isInteger(body.days) || body.days < 1 || body.days > 60) {
      return c.json({ error: "\"days\" must be an integer from 1 to 60" }, 400);
    }
    const template = store.listTemplates().find((t) => t.id === body.template);
    if (!template) {
      const known = store.listTemplates().map((t) => t.id).join(", ");
      return c.json({ error: `unknown template, choose one of: ${known}` }, 400);
    }
    return c.json(summarize(store.createTrip(body.name.trim(), body.days, template)), 201);
  });

  app.get("/api/trips/:id", (c) => {
    const trip = store.getTrip(c.req.param("id"));
    if (!trip) return c.json({ error: `trip "${c.req.param("id")}" not found` }, 404);
    return c.json(summarize(trip));
  });

  app.post("/api/trips/:id/items", async (c) => {
    const body = await c.req.json().catch(() => null);
    if (!body || typeof body.name !== "string" || !body.name.trim() || typeof body.category !== "string") {
      return c.json({ error: "\"name\" and \"category\" are required strings" }, 400);
    }
    const qty = body.qty ?? 1;
    if (!Number.isInteger(qty) || qty < 1 || qty > 99) {
      return c.json({ error: "\"qty\" must be an integer from 1 to 99" }, 400);
    }
    return c.json(store.addItem(c.req.param("id"), { name: body.name.trim(), category: body.category, qty }), 201);
  });

  app.patch("/api/trips/:id/items/:itemId", async (c) => {
    const body = await c.req.json().catch(() => null);
    if (!body || typeof body.packed !== "boolean") {
      return c.json({ error: "body must include boolean \"packed\"" }, 400);
    }
    return c.json(store.setPacked(c.req.param("id"), c.req.param("itemId"), body.packed));
  });

  app.delete("/api/trips/:id/items/:itemId", (c) => {
    store.removeItem(c.req.param("id"), c.req.param("itemId"));
    return c.body(null, 204);
  });

  return app;
}
