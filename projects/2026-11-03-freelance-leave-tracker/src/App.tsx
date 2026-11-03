import { useEffect, useState } from 'react'
import { leaveSource } from './leaveSource'
import { balance, competing, conflicts, isWeekend, monthDays, parse, workingDays } from './leave'
import type { LeaveData, LeaveRequest } from './types'

const fmt = (d: string) =>
  parse(d).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', timeZone: 'UTC' })

const chip: Record<string, string> = {
  pending: 'bg-amber-100 text-amber-800',
  approved: 'bg-emerald-100 text-emerald-800',
  rejected: 'bg-rose-100 text-rose-800',
}

export default function App() {
  const [data, setData] = useState<LeaveData | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    leaveSource.load().then(setData, (e: Error) => setError(e.message))
  }, [])

  async function decide(id: string, status: 'approved' | 'rejected') {
    try {
      const updated = await leaveSource.decide(id, status)
      setData((d) => d && { ...d, requests: d.requests.map((r) => (r.id === id ? updated : r)) })
    } catch (e) {
      setError((e as Error).message)
    }
  }

  if (error) return <p className="p-8 text-rose-700">{error}</p>
  if (!data) return <p className="p-8">Loading…</p>

  const { employees, requests, holidays } = data
  const name = (id: string) => employees.find((e) => e.id === id)!
  const pending = requests.filter((r) => r.status === 'pending')

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <header className="flex items-end justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Leave Tracker</h1>
          <p className="text-sm text-slate-500">Manager view · November 2026</p>
        </div>
        <span className="rounded-full bg-amber-100 px-3 py-1 text-sm font-medium text-amber-800">
          {pending.length} awaiting decision
        </span>
      </header>

      <div className="grid gap-6 lg:grid-cols-5">
        <section className="space-y-3 lg:col-span-3">
          <h2 className="font-medium">Approval queue</h2>
          {pending.length === 0 && <p className="text-sm text-slate-500">Nothing left to review.</p>}
          {pending.map((r) => (
            <RequestCard key={r.id} req={r} data={data} onDecide={decide} />
          ))}
          <h2 className="pt-2 font-medium">Decided</h2>
          <ul className="divide-y rounded-lg border bg-white text-sm">
            {requests
              .filter((r) => r.status !== 'pending')
              .map((r) => (
                <li key={r.id} className="flex items-center justify-between px-4 py-2">
                  <span>
                    {name(r.employeeId).name} · {fmt(r.from)}–{fmt(r.to)} · {r.type}
                  </span>
                  <span className={`rounded-full px-2 py-0.5 text-xs ${chip[r.status]}`}>{r.status}</span>
                </li>
              ))}
          </ul>
        </section>

        <section className="space-y-6 lg:col-span-2">
          <Calendar data={data} />
          <div>
            <h2 className="mb-2 font-medium">Balances (working days left)</h2>
            <table className="w-full overflow-hidden rounded-lg border bg-white text-sm">
              <thead className="bg-slate-100 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-3 py-2">Employee</th>
                  <th className="px-3 py-2">Annual</th>
                  <th className="px-3 py-2">Sick</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((e) => {
                  const a = balance(e, 'annual', requests, holidays)
                  const s = balance(e, 'sick', requests, holidays)
                  return (
                    <tr key={e.id} className="border-t">
                      <td className="px-3 py-2">{e.name}</td>
                      <td className="px-3 py-2">
                        {a.remaining}/{a.allowance}
                        {a.pending > 0 && <span className="text-amber-700"> (+{a.pending} pending)</span>}
                      </td>
                      <td className="px-3 py-2">
                        {s.remaining}/{s.allowance}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  )
}

function RequestCard({
  req,
  data,
  onDecide,
}: {
  req: LeaveRequest
  data: LeaveData
  onDecide: (id: string, s: 'approved' | 'rejected') => void
}) {
  const { employees, requests, holidays } = data
  const emp = employees.find((e) => e.id === req.employeeId)!
  const days = workingDays(req.from, req.to, holidays)
  const bal = balance(emp, req.type, requests, holidays)
  const over = days > bal.remaining
  const clash = conflicts(req, employees, requests)
  const rivals = competing(req, employees, requests)

  return (
    <article className="rounded-lg border bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="font-medium">
            {emp.name} <span className="text-sm font-normal text-slate-500">· {emp.team}</span>
          </p>
          <p className="text-sm text-slate-600">
            {fmt(req.from)} – {fmt(req.to)} · {days} working day{days === 1 ? '' : 's'} · {req.type}
          </p>
          <p className="text-sm italic text-slate-500">“{req.reason}”</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => onDecide(req.id, 'rejected')}
            className="rounded-md border border-rose-300 px-3 py-1 text-sm text-rose-700 hover:bg-rose-50"
          >
            Reject
          </button>
          <button
            onClick={() => onDecide(req.id, 'approved')}
            disabled={over}
            className="rounded-md bg-emerald-600 px-3 py-1 text-sm text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Approve
          </button>
        </div>
      </div>
      <ul className="mt-2 space-y-1 text-xs">
        {over && (
          <li className="text-rose-700">
            ⛔ Needs {days} days but only {bal.remaining} {req.type} left — cannot approve.
          </li>
        )}
        {clash.length > 0 && (
          <li className="text-amber-700">⚠ {clash.map((c) => c.name).join(', ')} already off (approved) in {emp.team}.</li>
        )}
        {rivals.length > 0 && (
          <li className="text-amber-700">⚠ Also requested by {rivals.map((c) => c.name).join(', ')} in {emp.team}.</li>
        )}
        {!over && clash.length === 0 && rivals.length === 0 && <li className="text-emerald-700">✓ No conflicts</li>}
      </ul>
    </article>
  )
}

function Calendar({ data }: { data: LeaveData }) {
  const { month, employees, requests, holidays } = data
  const cells = monthDays(month)
  const heads = ['M', 'T', 'W', 'T', 'F', 'S', 'S']

  return (
    <div>
      <h2 className="mb-2 font-medium">Team calendar</h2>
      <div className="grid grid-cols-7 gap-1 rounded-lg border bg-white p-2 text-center text-xs">
        {heads.map((h, i) => (
          <div key={i} className="font-medium text-slate-400">
            {h}
          </div>
        ))}
        {cells.map((d, i) => {
          if (!d) return <div key={i} />
          const off = (status: string) =>
            requests.filter((r) => r.status === status && r.from <= d && r.to >= d).length
          const approved = off('approved')
          const pend = off('pending')
          const shade = isWeekend(parse(d)) || holidays.includes(d) ? 'bg-slate-100 text-slate-400' : ''
          return (
            <div key={d} title={`${approved} approved, ${pend} pending of ${employees.length}`} className={`rounded p-1 ${shade}`}>
              <div>{Number(d.slice(8))}</div>
              <div className="flex h-2 justify-center gap-0.5">
                {approved > 0 && <span className="h-2 rounded-full bg-emerald-500 px-1 text-[0px]" style={{ width: 4 + approved * 4 }} />}
                {pend > 0 && <span className="h-2 rounded-full bg-amber-400" style={{ width: 4 + pend * 4 }} />}
              </div>
            </div>
          )
        })}
      </div>
      <p className="mt-1 text-xs text-slate-500">
        <span className="text-emerald-600">●</span> approved <span className="text-amber-500">●</span> pending · grey =
        weekend / public holiday
      </p>
    </div>
  )
}
