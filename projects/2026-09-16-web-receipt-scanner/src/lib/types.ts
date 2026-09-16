export interface ScannedLine {
  name: string
  price: number
  confidence: number
}

export interface ScannedReceipt {
  id: string
  label: string
  merchant: string
  date: string
  lines: ScannedLine[]
  taxRate: number
}
