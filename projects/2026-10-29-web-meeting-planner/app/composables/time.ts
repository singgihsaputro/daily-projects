import type { City } from '~/services/cityService'

const formatters = new Map<string, Intl.DateTimeFormat>()

function formatter(zone: string) {
  let f = formatters.get(zone)
  if (!f) {
    f = new Intl.DateTimeFormat('en-GB', {
      timeZone: zone, hourCycle: 'h23', year: 'numeric', month: 'numeric',
      day: 'numeric', hour: 'numeric', minute: 'numeric'
    })
    formatters.set(zone, f)
  }
  return f
}

export function localParts(instant: number, zone: string) {
  const p: Record<string, number> = {}
  for (const part of formatter(zone).formatToParts(instant)) {
    if (part.type !== 'literal') p[part.type] = Number(part.value)
  }
  return p as { year: number; month: number; day: number; hour: number; minute: number }
}

/** Offset of `zone` from UTC in minutes at the given instant. */
export function offsetMinutes(instant: number, zone: string) {
  const p = localParts(instant, zone)
  return Math.round((Date.UTC(p.year, p.month - 1, p.day, p.hour, p.minute) - instant) / 60000)
}

export function formatOffset(minutes: number) {
  const sign = minutes < 0 ? '-' : '+'
  const abs = Math.abs(minutes)
  const m = abs % 60
  return `UTC${sign}${Math.floor(abs / 60)}${m ? ':' + String(m).padStart(2, '0') : ''}`
}

/** The 24 hourly instants of `date` (YYYY-MM-DD) in the home zone. */
export function daySlots(date: string, homeZone: string) {
  const [y, m, d] = date.split('-').map(Number)
  const guess = Date.UTC(y!, m! - 1, d!)
  const start = guess - offsetMinutes(guess, homeZone) * 60000
  return Array.from({ length: 24 }, (_, i) => start + i * 3600000)
}

export function isWorking(instant: number, city: City) {
  const h = localParts(instant, city.zone).hour
  return h >= city.workStart && h < city.workEnd
}

export function clock(instant: number, zone: string) {
  const p = localParts(instant, zone)
  return `${String(p.hour).padStart(2, '0')}:${String(p.minute).padStart(2, '0')}`
}

export function dayLabel(instant: number, zone: string) {
  return new Intl.DateTimeFormat('en-GB', { timeZone: zone, weekday: 'short', day: 'numeric', month: 'short' })
    .format(instant)
}
