import type { Item } from '../types'
import fixture from '../../mock/inventory.json'

/** The only surface the UI uses to read or change stock. */
export interface InventoryRepository {
  list(): Promise<Item[]>
  setStock(sku: string, stock: number): Promise<Item>
}

/** Fixture-backed implementation. Swap this file's export for an HTTP client to go live. */
class MockInventoryRepository implements InventoryRepository {
  private items: Item[] = structuredClone(fixture as Item[])

  async list(): Promise<Item[]> {
    return structuredClone(this.items)
  }

  async setStock(sku: string, stock: number): Promise<Item> {
    const item = this.items.find((i) => i.sku === sku)
    if (!item) throw new Error(`Unknown SKU ${sku}`)
    item.stock = Math.max(0, Math.floor(stock))
    return { ...item }
  }
}

export const inventory: InventoryRepository = new MockInventoryRepository()
