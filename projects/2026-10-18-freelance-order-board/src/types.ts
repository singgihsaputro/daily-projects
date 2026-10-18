export type Status = 'new' | 'preparing' | 'ready' | 'served'

export interface OrderItem {
  name: string
  qty: number
}

export interface Order {
  id: number
  table: string
  status: Status
  placedMinutesAgo: number
  note: string
  items: OrderItem[]
}

export const COLUMNS: { status: Status; label: string; action: string }[] = [
  { status: 'new', label: 'New', action: 'Start cooking' },
  { status: 'preparing', label: 'Preparing', action: 'Mark ready' },
  { status: 'ready', label: 'Ready to serve', action: 'Mark served' },
  { status: 'served', label: 'Served', action: '' },
]
