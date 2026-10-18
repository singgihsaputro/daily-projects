import { COLUMNS, type Order, type Status } from './types'

export function nextStatus(status: Status): Status | null {
  const i = COLUMNS.findIndex((c) => c.status === status)
  return i >= 0 && i < COLUMNS.length - 1 ? COLUMNS[i + 1].status : null
}

export type Urgency = 'ok' | 'slow' | 'late'

/** Open orders get flagged by age: 10+ min is slow, 20+ min is late. */
export function urgency(order: Order): Urgency {
  if (order.status === 'served') return 'ok'
  if (order.placedMinutesAgo >= 20) return 'late'
  if (order.placedMinutesAgo >= 10) return 'slow'
  return 'ok'
}

export function itemCount(order: Order): number {
  return order.items.reduce((sum, i) => sum + i.qty, 0)
}

export function matchesQuery(order: Order, query: string): boolean {
  const q = query.trim().toLowerCase()
  if (!q) return true
  return (
    order.table.toLowerCase().includes(q) ||
    String(order.id).includes(q) ||
    order.items.some((i) => i.name.toLowerCase().includes(q))
  )
}
