import type { Filter, Item, StockStatus } from './types'

export function statusOf(item: Item): StockStatus {
  if (item.stock === 0) return 'out'
  if (item.stock <= item.reorderLevel) return 'low'
  return 'ok'
}

/** Whole reorder batches needed to climb back above the reorder level. */
export function suggestedOrderQty(item: Item): number {
  if (statusOf(item) === 'ok') return 0
  const batches = Math.floor((item.reorderLevel - item.stock) / item.reorderQty) + 1
  return batches * item.reorderQty
}

export function matches(item: Item, filter: Filter, query: string): boolean {
  const status = statusOf(item)
  if (filter === 'attention' && status === 'ok') return false
  if ((filter === 'out' || filter === 'low' || filter === 'ok') && status !== filter) return false
  const q = query.trim().toLowerCase()
  return !q || `${item.sku} ${item.name} ${item.category}`.toLowerCase().includes(q)
}

export const rupiah = (n: number): string =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(n)
