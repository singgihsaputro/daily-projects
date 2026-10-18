import orders from '../mock/orders.json'
import type { Order, Status } from './types'

/** Everything the board needs from a backend. Swap this file's export for a real client. */
export interface OrderSource {
  list(): Promise<Order[]>
  setStatus(id: number, status: Status): Promise<Order>
}

export function createMockSource(): OrderSource {
  let data: Order[] = (orders as Order[]).map((o) => ({ ...o, items: [...o.items] }))
  return {
    async list() {
      return data.map((o) => ({ ...o }))
    },
    async setStatus(id, status) {
      const found = data.find((o) => o.id === id)
      if (!found) throw new Error(`Order ${id} not found`)
      found.status = status
      return { ...found }
    },
  }
}

export const orderSource: OrderSource = createMockSource()
