import type { CartLine, Product, Totals } from './types.ts'

export const TAX_RATE = 0.11

export function formatRupiah(n: number): string {
  return 'Rp ' + Math.round(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

export function addToCart(lines: CartLine[], product: Product): CartLine[] {
  const hit = lines.find((l) => l.product.sku === product.sku)
  if (!hit) return [...lines, { product, qty: 1 }]
  return lines.map((l) => (l === hit ? { ...l, qty: l.qty + 1 } : l))
}

// Setting a quantity of zero or less removes the line.
export function setQty(lines: CartLine[], sku: string, qty: number): CartLine[] {
  return lines
    .map((l) => (l.product.sku === sku ? { ...l, qty } : l))
    .filter((l) => l.qty > 0)
}

export function totals(lines: CartLine[]): Totals {
  const subtotal = lines.reduce((s, l) => s + l.product.price * l.qty, 0)
  const tax = Math.round(subtotal * TAX_RATE)
  return { subtotal, tax, total: subtotal + tax }
}

// Cash is rounded up to the nearest Rp 100 when no coins are in the till.
export function roundCash(total: number): number {
  return Math.ceil(total / 100) * 100
}

export function quickCash(total: number): number[] {
  const due = roundCash(total)
  const notes = [due, 20000, 50000, 100000, 200000].filter((n) => n >= due)
  return [...new Set(notes)].slice(0, 4)
}

export function matches(p: Product, query: string): boolean {
  const q = query.trim().toLowerCase()
  return q === '' || p.name.toLowerCase().includes(q) || p.sku.includes(q)
}
