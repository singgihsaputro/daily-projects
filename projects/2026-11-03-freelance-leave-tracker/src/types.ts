export type LeaveType = 'annual' | 'sick'
export type Status = 'pending' | 'approved' | 'rejected'

export interface Employee {
  id: string
  name: string
  team: string
  annual: number
  sick: number
}

export interface LeaveRequest {
  id: string
  employeeId: string
  type: LeaveType
  from: string
  to: string
  reason: string
  status: Status
}

export interface LeaveData {
  month: string
  employees: Employee[]
  holidays: string[]
  requests: LeaveRequest[]
}
