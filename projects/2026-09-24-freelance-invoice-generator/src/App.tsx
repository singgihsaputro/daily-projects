import { useEffect, useMemo, useState } from 'react'
import { invoiceDataProvider } from './lib/invoiceDataProvider'
import { buildInvoicePdf, calculateTotals, formatCurrency, lineItemAmount } from './lib/pdf'
import type { BusinessProfile, Client, LineItem } from './lib/types'

const todayIso = () => new Date().toISOString().slice(0, 10)

const addDaysIso = (days: number) => {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return d.toISOString().slice(0, 10)
}

const formatIso = (iso: string) =>
  new Date(`${iso}T00:00:00`).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })

let itemSeq = 0
const newItem = (): LineItem => ({
  id: `item-${++itemSeq}`,
  description: '',
  quantity: 1,
  unitPrice: 0,
})

function App() {
  const [business, setBusiness] = useState<BusinessProfile | null>(null)
  const [clients, setClients] = useState<Client[]>([])
  const [error, setError] = useState<string | null>(null)

  const [clientId, setClientId] = useState('')
  const [issueDate, setIssueDate] = useState(todayIso())
  const [dueDate, setDueDate] = useState(addDaysIso(14))
  const [taxRatePercent, setTaxRatePercent] = useState(0)
  const [notes, setNotes] = useState('Thank you for your business. Payment due within 14 days.')
  const [items, setItems] = useState<LineItem[]>([newItem(), newItem()])

  useEffect(() => {
    Promise.all([invoiceDataProvider.getBusinessProfile(), invoiceDataProvider.getClients()])
      .then(([profile, clientList]) => {
        setBusiness(profile)
        setClients(clientList)
        setTaxRatePercent(profile.defaultTaxRatePercent)
        setClientId(clientList[0]?.id ?? '')
      })
      .catch((err: Error) => setError(err.message))
  }, [])

  const client = useMemo(() => clients.find((c) => c.id === clientId) ?? null, [clients, clientId])

  const invoiceNumber = useMemo(() => {
    if (!business) return ''
    const year = issueDate.slice(0, 4)
    return `${business.invoicePrefix}-${year}-${String(business.nextInvoiceSequence).padStart(4, '0')}`
  }, [business, issueDate])

  const totals = useMemo(() => calculateTotals(items, taxRatePercent), [items, taxRatePercent])

  const updateItem = (id: string, patch: Partial<LineItem>) =>
    setItems((prev) => prev.map((item) => (item.id === id ? { ...item, ...patch } : item)))

  const removeItem = (id: string) => setItems((prev) => prev.filter((item) => item.id !== id))

  const canDownload = business && client && items.some((item) => item.description.trim())

  const downloadPdf = () => {
    if (!business || !client) return
    const doc = buildInvoicePdf({
      business,
      client,
      invoiceNumber,
      issueDate: formatIso(issueDate),
      dueDate: formatIso(dueDate),
      items,
      taxRatePercent,
      notes,
    })
    doc.save(`${invoiceNumber}.pdf`)
  }

  if (error) {
    return <div className="p-8 text-red-600">Failed to load invoice data: {error}</div>
  }

  if (!business) {
    return <div className="p-8 text-slate-500">Loading...</div>
  }

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-8">
      <div className="mx-auto max-w-3xl space-y-6">
        <header>
          <h1 className="text-2xl font-semibold text-slate-900">Invoice Generator</h1>
          <p className="text-sm text-slate-500">{business.name} &middot; {invoiceNumber}</p>
        </header>

        <section className="grid grid-cols-1 gap-4 rounded-lg border border-slate-200 bg-white p-5 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Bill to</span>
            <select
              className="w-full rounded border border-slate-300 px-3 py-2"
              value={clientId}
              onChange={(e) => setClientId(e.target.value)}
            >
              {clients.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Tax rate (%)</span>
            <input
              type="number"
              min={0}
              max={100}
              className="w-full rounded border border-slate-300 px-3 py-2"
              value={taxRatePercent}
              onChange={(e) => setTaxRatePercent(Number(e.target.value))}
            />
          </label>

          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Issue date</span>
            <input
              type="date"
              className="w-full rounded border border-slate-300 px-3 py-2"
              value={issueDate}
              onChange={(e) => setIssueDate(e.target.value)}
            />
          </label>

          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Due date</span>
            <input
              type="date"
              className="w-full rounded border border-slate-300 px-3 py-2"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
            />
          </label>

          {client && (
            <div className="sm:col-span-2 rounded border border-slate-100 bg-slate-50 p-3 text-sm text-slate-600">
              {client.contact} &middot; {client.address} &middot; {client.email}
            </div>
          )}
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-medium text-slate-700">Line items</h2>
            <button
              type="button"
              className="rounded bg-slate-800 px-3 py-1.5 text-sm text-white hover:bg-slate-700"
              onClick={() => setItems((prev) => [...prev, newItem()])}
            >
              Add item
            </button>
          </div>

          <div className="space-y-2">
            {items.map((item) => (
              <div key={item.id} className="grid grid-cols-12 items-center gap-2">
                <input
                  className="col-span-5 rounded border border-slate-300 px-2 py-1.5 text-sm"
                  placeholder="Description"
                  value={item.description}
                  onChange={(e) => updateItem(item.id, { description: e.target.value })}
                />
                <input
                  type="number"
                  min={0}
                  className="col-span-1 rounded border border-slate-300 px-2 py-1.5 text-sm"
                  value={item.quantity}
                  onChange={(e) => updateItem(item.id, { quantity: Number(e.target.value) })}
                />
                <input
                  type="number"
                  min={0}
                  className="col-span-2 rounded border border-slate-300 px-2 py-1.5 text-sm"
                  value={item.unitPrice}
                  onChange={(e) => updateItem(item.id, { unitPrice: Number(e.target.value) })}
                />
                <span className="col-span-3 text-right text-sm text-slate-600">
                  {formatCurrency(lineItemAmount(item))}
                </span>
                <button
                  type="button"
                  aria-label="Remove item"
                  className="col-span-1 text-right text-sm text-red-500 hover:text-red-700"
                  onClick={() => removeItem(item.id)}
                >
                  &times;
                </button>
              </div>
            ))}
          </div>

          <div className="mt-4 ml-auto w-56 space-y-1 text-sm">
            <div className="flex justify-between text-slate-600">
              <span>Subtotal</span>
              <span>{formatCurrency(totals.subtotal)}</span>
            </div>
            <div className="flex justify-between text-slate-600">
              <span>Tax ({taxRatePercent}%)</span>
              <span>{formatCurrency(totals.tax)}</span>
            </div>
            <div className="flex justify-between border-t border-slate-200 pt-1 text-base font-semibold text-slate-900">
              <span>Total</span>
              <span>{formatCurrency(totals.total)}</span>
            </div>
          </div>
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-5">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Notes</span>
            <textarea
              className="w-full rounded border border-slate-300 px-3 py-2"
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </label>
        </section>

        <button
          type="button"
          disabled={!canDownload}
          className="w-full rounded-lg bg-indigo-600 py-3 font-medium text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-slate-300"
          onClick={downloadPdf}
        >
          Download PDF
        </button>
      </div>
    </div>
  )
}

export default App
