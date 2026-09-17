export interface Employee {
  id: string;
  name: string;
  role: string;
}

export interface Shift {
  id: string;
  date: string; // YYYY-MM-DD
  start: string; // HH:MM, 24h
  end: string; // HH:MM, 24h
  role: string;
  employeeId: string | null;
}
