import type { BusinessProfile, Client } from './types'

/**
 * Everything the invoice screen needs from "the outside world". Swapping the
 * mock fixtures below for a real backend means writing one class that
 * implements this interface and pointing `invoiceDataProvider` at it -
 * nothing in the UI has to change.
 */
export interface InvoiceDataProvider {
  getBusinessProfile(): Promise<BusinessProfile>
  getClients(): Promise<Client[]>
}

class MockInvoiceDataProvider implements InvoiceDataProvider {
  async getBusinessProfile(): Promise<BusinessProfile> {
    const res = await fetch('/mock/business.json')
    if (!res.ok) throw new Error(`Failed to load business profile: ${res.status}`)
    return res.json()
  }

  async getClients(): Promise<Client[]> {
    const res = await fetch('/mock/clients.json')
    if (!res.ok) throw new Error(`Failed to load clients: ${res.status}`)
    return res.json()
  }
}

export const invoiceDataProvider: InvoiceDataProvider = new MockInvoiceDataProvider()
