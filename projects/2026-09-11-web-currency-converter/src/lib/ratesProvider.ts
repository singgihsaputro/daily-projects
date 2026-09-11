export interface RatesData {
  base: string
  updatedAt: string
  rates: Record<string, number>
}

export interface RatesProvider {
  getRates(): Promise<RatesData>
}

/**
 * Reads exchange rates from the bundled fixture instead of a live API.
 * Swap this for a provider that fetches a real endpoint by changing
 * only this file — callers depend on `RatesProvider`, not on this class.
 */
export class MockRatesProvider implements RatesProvider {
  async getRates(): Promise<RatesData> {
    const res = await fetch('/mock/rates.json')
    if (!res.ok) {
      throw new Error(`Failed to load rates: ${res.status}`)
    }
    return res.json() as Promise<RatesData>
  }
}
