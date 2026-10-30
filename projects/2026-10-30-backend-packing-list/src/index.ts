import { serve } from "@hono/node-server";
import { fileURLToPath } from "node:url";
import { buildApp } from "./app.js";
import { JsonPackingStore } from "./store.js";

const mock = (name: string) => fileURLToPath(new URL(`../mock/${name}`, import.meta.url));
const store = await JsonPackingStore.load(mock("templates.json"), mock("trips.json"));
const port = Number(process.env.PORT ?? 8080);

serve({ fetch: buildApp(store).fetch, port });
console.log(`packing-list listening on http://localhost:${port}`);
