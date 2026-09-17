import { readFile } from "node:fs/promises";
import type { Employee, Shift } from "./types.js";

export interface ShiftStore {
  listEmployees(): Employee[];
  getEmployee(id: string): Employee | undefined;
  listShifts(date?: string): Shift[];
  getShift(id: string): Shift | undefined;
  assign(shiftId: string, employeeId: string): Shift;
  unassign(shiftId: string): Shift;
}

export class ConflictError extends Error {}
export class NotFoundError extends Error {}

// Reads seed data from mock/*.json on load, then serves an in-memory copy.
// Swapping to a real database later means replacing this one file.
export class JsonShiftStore implements ShiftStore {
  private employees: Employee[];
  private shifts: Shift[];

  private constructor(employees: Employee[], shifts: Shift[]) {
    this.employees = employees;
    this.shifts = shifts;
  }

  static async load(employeesPath: string, shiftsPath: string): Promise<JsonShiftStore> {
    const [employeesRaw, shiftsRaw] = await Promise.all([
      readFile(employeesPath, "utf-8"),
      readFile(shiftsPath, "utf-8"),
    ]);
    return new JsonShiftStore(JSON.parse(employeesRaw), JSON.parse(shiftsRaw));
  }

  listEmployees(): Employee[] {
    return this.employees;
  }

  getEmployee(id: string): Employee | undefined {
    return this.employees.find((e) => e.id === id);
  }

  listShifts(date?: string): Shift[] {
    return date ? this.shifts.filter((s) => s.date === date) : this.shifts;
  }

  getShift(id: string): Shift | undefined {
    return this.shifts.find((s) => s.id === id);
  }

  assign(shiftId: string, employeeId: string): Shift {
    const shift = this.getShift(shiftId);
    if (!shift) throw new NotFoundError(`shift "${shiftId}" not found`);

    const employee = this.getEmployee(employeeId);
    if (!employee) throw new NotFoundError(`employee "${employeeId}" not found`);

    if (employee.role !== shift.role) {
      throw new ConflictError(
        `${employee.name} is a ${employee.role}, but shift "${shiftId}" needs a ${shift.role}`,
      );
    }

    const overlap = this.shifts.find(
      (s) =>
        s.id !== shift.id &&
        s.employeeId === employeeId &&
        s.date === shift.date &&
        s.start < shift.end &&
        shift.start < s.end,
    );
    if (overlap) {
      throw new ConflictError(
        `${employee.name} already has shift "${overlap.id}" (${overlap.start}-${overlap.end}) on ${overlap.date}`,
      );
    }

    shift.employeeId = employeeId;
    return shift;
  }

  unassign(shiftId: string): Shift {
    const shift = this.getShift(shiftId);
    if (!shift) throw new NotFoundError(`shift "${shiftId}" not found`);
    shift.employeeId = null;
    return shift;
  }
}
