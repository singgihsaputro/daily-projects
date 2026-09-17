import { serve } from "@hono/node-server";
import { Hono } from "hono";
import { ConflictError, JsonShiftStore, NotFoundError, type ShiftStore } from "./store.js";

export function buildApp(store: ShiftStore): Hono {
  const app = new Hono();

  app.get("/api/employees", (c) => c.json(store.listEmployees()));

  app.get("/api/employees/:id/schedule", (c) => {
    const id = c.req.param("id");
    const employee = store.getEmployee(id);
    if (!employee) return c.json({ error: `employee "${id}" not found` }, 404);
    const shifts = store.listShifts().filter((s) => s.employeeId === id);
    return c.json({ employee, shifts });
  });

  app.get("/api/shifts", (c) => {
    const date = c.req.query("date");
    return c.json(store.listShifts(date));
  });

  app.post("/api/shifts/:id/assign", async (c) => {
    const id = c.req.param("id");
    const body = await c.req.json().catch(() => null);
    if (!body || typeof body.employeeId !== "string") {
      return c.json({ error: "body must include \"employeeId\"" }, 400);
    }
    try {
      const shift = store.assign(id, body.employeeId);
      return c.json(shift);
    } catch (err) {
      if (err instanceof NotFoundError) return c.json({ error: err.message }, 404);
      if (err instanceof ConflictError) return c.json({ error: err.message }, 409);
      throw err;
    }
  });

  app.delete("/api/shifts/:id/assign", (c) => {
    const id = c.req.param("id");
    try {
      const shift = store.unassign(id);
      return c.json(shift);
    } catch (err) {
      if (err instanceof NotFoundError) return c.json({ error: err.message }, 404);
      throw err;
    }
  });

  return app;
}

async function main() {
  const store = await JsonShiftStore.load("mock/employees.json", "mock/shifts.json");
  const app = buildApp(store);
  const port = Number(process.env.PORT ?? 8080);
  console.log(`shift-planner listening on :${port}`);
  serve({ fetch: app.fetch, port });
}

if (process.argv[1] && process.argv[1].endsWith("index.ts")) {
  main();
}
