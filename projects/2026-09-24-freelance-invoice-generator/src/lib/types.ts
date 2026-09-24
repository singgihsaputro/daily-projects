export interface BusinessProfile {
  name: string
  tagline: string
  email: string
  phone: string
  address: string
  bankName: string
  bankAccount: string
  bankHolder: string
  defaultTaxRatePercent: number
  invoicePrefix: string
  nextInvoiceSequence: number
}

export interface Client {
  id: string
  name: string
  contact: string
  email: string
  address: string
  taxId: string
}

export interface LineItem {
  id: string
  description: string
  quantity: number
  unitPrice: number
}
