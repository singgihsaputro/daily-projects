import data from '../mock/leave.json'
import type { LeaveData, LeaveRequest, Status } from './types'

/** The only seam to the backend. Swap this file's implementation for a fetch client. */
export interface LeaveSource {
  load(): Promise<LeaveData>
  decide(id: string, status: Exclude<Status, 'pending'>): Promise<LeaveRequest>
}

export function createMockSource(): LeaveSource {
  const state: LeaveData = structuredClone(data) as LeaveData
  return {
    async load() {
      return structuredClone(state)
    },
    async decide(id, status) {
      const req = state.requests.find((r) => r.id === id)
      if (!req) throw new Error(`Unknown request ${id}`)
      req.status = status
      return { ...req }
    },
  }
}

export const leaveSource: LeaveSource = createMockSource()
