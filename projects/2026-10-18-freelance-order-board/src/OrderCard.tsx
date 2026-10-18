import { itemCount, nextStatus, urgency } from './board'
import { COLUMNS, type Order } from './types'

const BADGE = {
  ok: 'bg-stone-100 text-stone-600',
  slow: 'bg-amber-100 text-amber-800',
  late: 'bg-red-100 text-red-700',
}

export default function OrderCard({ order, onAdvance }: { order: Order; onAdvance: (o: Order) => void }) {
  const level = urgency(order)
  const action = COLUMNS.find((c) => c.status === order.status)?.action ?? ''

  return (
    <article className="rounded-xl border border-stone-200 bg-white p-4 shadow-sm" data-testid="order-card">
      <header className="flex items-center justify-between">
        <h3 className="text-lg font-semibold">
          {order.table} <span className="text-sm font-normal text-stone-400">#{order.id}</span>
        </h3>
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${BADGE[level]}`}>
          {order.placedMinutesAgo} min
        </span>
      </header>
      <ul className="mt-3 space-y-1 text-sm">
        {order.items.map((i) => (
          <li key={i.name} className="flex justify-between">
            <span>{i.name}</span>
            <span className="font-medium text-stone-500">×{i.qty}</span>
          </li>
        ))}
      </ul>
      {order.note && (
        <p className="mt-3 rounded-md bg-amber-50 px-2 py-1 text-xs text-amber-900">{order.note}</p>
      )}
      <footer className="mt-3 flex items-center justify-between text-xs text-stone-400">
        <span>{itemCount(order)} items</span>
        {nextStatus(order.status) && (
          <button
            onClick={() => onAdvance(order)}
            className="rounded-lg bg-stone-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-stone-700"
          >
            {action}
          </button>
        )}
      </footer>
    </article>
  )
}
