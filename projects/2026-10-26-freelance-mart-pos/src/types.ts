export interface Product {
  sku: string
  name: string
  price: number
  category: string
}

export interface CartLine {
  product: Product
  qty: number
}

export interface Totals {
  subtotal: number
  tax: number
  total: number
}

export interface Sale {
  receiptNo: string
  paidAt: Date
  lines: CartLine[]
  totals: Totals
  tendered: number
  change: number
}
