import type { Employee, LeaveRequest, LeaveType } from './types'

const DAY = 86_400_000

export const parse = (iso: string) => new Date(iso + 'T00:00:00Z')
export const iso = (d: Date) => d.toISOString().slice(0, 10)
export const isWeekend = (d: Date) => d.getUTCDay() === 0 || d.getUTCDay() === 6

/** Every calendar date from `from` to `to`, inclusive. */
export function eachDay(from: string, to: string): string[] {
  const out: string[] = []
  for (let t = parse(from).getTime(); t <= parse(to).getTime(); t += DAY) out.push(iso(new Date(t)))
  return out
}

/** Working days in a range: weekdays that are not public holidays. */
export function workingDays(from: string, to: string, holidays: string[]): number {
  return eachDay(from, to).filter((d) => !isWeekend(parse(d)) && !holidays.includes(d)).length
}

export interface Balance {
  type: LeaveType
  allowance: number
  used: number
  pending: number
  remaining: number
}

export function balance(emp: Employee, type: LeaveType, requests: LeaveRequest[], holidays: string[]): Balance {
  const sum = (status: string) =>
    requests
      .filter((r) => r.employeeId === emp.id && r.type === type && r.status === status)
      .reduce((n, r) => n + workingDays(r.from, r.to, holidays), 0)
  const used = sum('approved')
  const pending = sum('pending')
  return { type, allowance: emp[type], used, pending, remaining: emp[type] - used }
}

/** Other people on the same team whose approved leave overlaps this request. */
export function conflicts(req: LeaveRequest, employees: Employee[], requests: LeaveRequest[]): Employee[] {
  const me = employees.find((e) => e.id === req.employeeId)
  if (!me) return []
  return employees.filter(
    (e) =>
      e.id !== me.id &&
      e.team === me.team &&
      requests.some(
        (r) => r.employeeId === e.id && r.status === 'approved' && r.from <= req.to && r.to >= req.from,
      ),
  )
}

/** Same-team colleagues who also have a *pending* request overlapping this one. */
export function competing(req: LeaveRequest, employees: Employee[], requests: LeaveRequest[]): Employee[] {
  const me = employees.find((e) => e.id === req.employeeId)
  if (!me) return []
  return employees.filter(
    (e) =>
      e.id !== me.id &&
      e.team === me.team &&
      requests.some((r) => r.employeeId === e.id && r.status === 'pending' && r.from <= req.to && r.to >= req.from),
  )
}

/** Cells for a month grid: leading blanks (Monday first) then ISO dates. */
export function monthDays(month: string): (string | null)[] {
  const first = parse(month + '-01')
  const lead = (first.getUTCDay() + 6) % 7
  const next = new Date(Date.UTC(first.getUTCFullYear(), first.getUTCMonth() + 1, 1))
  const days = eachDay(iso(first), iso(new Date(next.getTime() - DAY)))
  return [...Array<null>(lead).fill(null), ...days]
}
