import { readFile } from "node:fs/promises";
import type { Item, Template, Trip } from "./types.js";

export class NotFoundError extends Error {}

export interface PackingStore {
  listTemplates(): Template[];
  listTrips(): Trip[];
  getTrip(id: string): Trip | undefined;
  createTrip(name: string, days: number, template: Template): Trip;
  addItem(tripId: string, item: Omit<Item, "id" | "packed">): Item;
  setPacked(tripId: string, itemId: string, packed: boolean): Item;
  removeItem(tripId: string, itemId: string): void;
}

// Seeds from mock/*.json, then serves an in-memory copy. A database-backed
// store would replace this one file.
export class JsonPackingStore implements PackingStore {
  private seq = 100;

  private constructor(private templates: Template[], private trips: Trip[]) {}

  static async load(templatesPath: string, tripsPath: string): Promise<JsonPackingStore> {
    const [t, r] = await Promise.all([readFile(templatesPath, "utf-8"), readFile(tripsPath, "utf-8")]);
    return new JsonPackingStore(JSON.parse(t), JSON.parse(r));
  }

  listTemplates(): Template[] {
    return this.templates;
  }

  listTrips(): Trip[] {
    return this.trips;
  }

  getTrip(id: string): Trip | undefined {
    return this.trips.find((t) => t.id === id);
  }

  createTrip(name: string, days: number, template: Template): Trip {
    const trip: Trip = { id: `trip${++this.seq}`, name, days, template: template.id, items: [] };
    for (const ti of template.items) {
      const scaled = ti.perDay !== undefined ? Math.min(Math.ceil(ti.perDay * days), ti.max ?? Infinity) : ti.qty ?? 1;
      trip.items.push({ id: `i${++this.seq}`, name: ti.name, category: ti.category, qty: Math.max(1, scaled), packed: false });
    }
    this.trips.push(trip);
    return trip;
  }

  addItem(tripId: string, item: Omit<Item, "id" | "packed">): Item {
    const created: Item = { ...item, id: `i${++this.seq}`, packed: false };
    this.require(tripId).items.push(created);
    return created;
  }

  setPacked(tripId: string, itemId: string, packed: boolean): Item {
    const item = this.require(tripId).items.find((i) => i.id === itemId);
    if (!item) throw new NotFoundError(`item "${itemId}" not found`);
    item.packed = packed;
    return item;
  }

  removeItem(tripId: string, itemId: string): void {
    const trip = this.require(tripId);
    const idx = trip.items.findIndex((i) => i.id === itemId);
    if (idx < 0) throw new NotFoundError(`item "${itemId}" not found`);
    trip.items.splice(idx, 1);
  }

  private require(tripId: string): Trip {
    const trip = this.getTrip(tripId);
    if (!trip) throw new NotFoundError(`trip "${tripId}" not found`);
    return trip;
  }
}
