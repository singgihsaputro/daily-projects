import { useEffect, useMemo, useState } from 'react'
import { MockRatesProvider, type RatesData } from './lib/ratesProvider'

const ratesProvider = new MockRatesProvider()

const CURRENCY_NAMES: Record<string, string> = {
  USD: 'US Dollar',
  EUR: 'Euro',
  GBP: 'British Pound',
  JPY: 'Japanese Yen',
  CAD: 'Canadian Dollar',
  AUD: 'Australian Dollar',
  CHF: 'Swiss Franc',
  CNY: 'Chinese Yuan',
  INR: 'Indian Rupee',
  BRL: 'Brazilian Real',
}

function convert(amount: number, from: string, to: string, rates: Record<string, number>): number {
  const usdAmount = amount / rates[from]
  return usdAmount * rates[to]
}

export default function App() {
  const [data, setData] = useState<RatesData | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [amount, setAmount] = useState('100')
  const [from, setFrom] = useState('USD')
  const [to, setTo] = useState('EUR')

  useEffect(() => {
    ratesProvider
      .getRates()
      .then(setData)
      .catch((err: Error) => setError(err.message))
  }, [])

  const result = useMemo(() => {
    if (!data) return null
    const parsed = Number(amount)
    if (!Number.isFinite(parsed)) return null
    return convert(parsed, from, to, data.rates)
  }, [data, amount, from, to])

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 text-red-400">
        Failed to load rates: {error}
      </div>
    )
  }

  if (!data) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 text-slate-400">
        Loading rates…
      </div>
    )
  }

  const currencies = Object.keys(data.rates).sort()

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center px-4 py-12">
      <div className="w-full max-w-md space-y-6">
        <header>
          <h1 className="text-2xl font-semibold">Currency Converter</h1>
          <p className="text-sm text-slate-400">
            Rates as of {new Date(data.updatedAt).toLocaleString()} (mock data)
          </p>
        </header>

        <div className="bg-slate-900 rounded-xl p-5 space-y-4 border border-slate-800">
          <label className="block">
            <span className="text-sm text-slate-400">Amount</span>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="mt-1 w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2 text-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>

          <div className="flex items-center gap-3">
            <label className="flex-1 block">
              <span className="text-sm text-slate-400">From</span>
              <select
                value={from}
                onChange={(e) => setFrom(e.target.value)}
                className="mt-1 w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2"
              >
                {currencies.map((c) => (
                  <option key={c} value={c}>
                    {c} — {CURRENCY_NAMES[c] ?? c}
                  </option>
                ))}
              </select>
            </label>

            <button
              type="button"
              onClick={() => {
                setFrom(to)
                setTo(from)
              }}
              aria-label="Swap currencies"
              className="mt-6 rounded-full bg-slate-800 border border-slate-700 h-9 w-9 flex items-center justify-center hover:bg-slate-700"
            >
              ⇄
            </button>

            <label className="flex-1 block">
              <span className="text-sm text-slate-400">To</span>
              <select
                value={to}
                onChange={(e) => setTo(e.target.value)}
                className="mt-1 w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2"
              >
                {currencies.map((c) => (
                  <option key={c} value={c}>
                    {c} — {CURRENCY_NAMES[c] ?? c}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="pt-2 border-t border-slate-800">
            <span className="text-sm text-slate-400">Converted amount</span>
            <p className="text-3xl font-semibold mt-1">
              {result !== null ? `${result.toFixed(2)} ${to}` : '—'}
            </p>
          </div>
        </div>

        <div className="bg-slate-900 rounded-xl p-5 border border-slate-800">
          <h2 className="text-sm font-medium text-slate-400 mb-3">
            All rates (base: {data.base})
          </h2>
          <ul className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
            {currencies.map((c) => (
              <li key={c} className="flex justify-between text-slate-300">
                <span>{c}</span>
                <span className="tabular-nums">{data.rates[c]}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
