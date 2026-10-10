export interface Item {
  sku: string
  name: string
  category: string
  supplier: string
  unitCost: number
  stock: number
  reorderLevel: number
  reorderQty: number
}

export type StockStatus = 'out' | 'low' | 'ok'
export type Filter = 'all' | 'attention' | StockStatus
