import { useEffect, useState } from 'react'
import { matchesQuery, nextStatus, urgency } from './board'
import { orderSource } from './orderSource'
import OrderCard from './OrderCard'
import { COLUMNS, type Order } from './types'

export default function App() {
  const [orders, setOrders] = useState<Order[]>([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    orderSource.list().then(setOrders).catch((e: Error) => setError(e.message))
  }, [])

  async function advance(order: Order) {
    const next = nextStatus(order.status)
    if (!next) return
    try {
      const updated = await orderSource.setStatus(order.id, next)
      setOrders((prev) => prev.map((o) => (o.id === updated.id ? updated : o)))
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const lateCount = orders.filter((o) => urgency(o) === 'late').length
  const open = orders.filter((o) => o.status !== 'served').length

  return (
    <main className="mx-auto max-w-7xl p-6">
      <header className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Warung Order Board</h1>
          <p className="text-sm text-stone-500">
            {open} open orders ·{' '}
            <span className={lateCount ? 'font-medium text-red-600' : ''}>{lateCount} late</span>
          </p>
        </div>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search table, order # or dish"
          aria-label="Search orders"
          className="w-72 rounded-lg border border-stone-300 bg-white px-3 py-2 text-sm"
        />
      </header>
      {error && <p role="alert" className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {COLUMNS.map((col) => {
          const list = orders.filter((o) => o.status === col.status && matchesQuery(o, query))
          return (
            <section key={col.status} aria-label={col.label} className="rounded-2xl bg-stone-200/60 p-3">
              <h2 className="mb-3 flex items-center justify-between px-1 text-sm font-semibold uppercase tracking-wide text-stone-600">
                {col.label}
                <span className="rounded-full bg-white px-2 py-0.5 text-xs">{list.length}</span>
              </h2>
              <div className="space-y-3">
                {list.map((o) => (
                  <OrderCard key={o.id} order={o} onAdvance={advance} />
                ))}
                {list.length === 0 && <p className="px-1 py-6 text-center text-sm text-stone-400">Nothing here</p>}
              </div>
            </section>
          )
        })}
      </div>
    </main>
  )
}
