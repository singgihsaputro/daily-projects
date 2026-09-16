import type { ScannedReceipt } from './types'

export interface OcrProvider {
  listScans(): Promise<ScannedReceipt[]>
  scan(id: string): Promise<ScannedReceipt>
}

/**
 * Stands in for a real OCR/receipt-parsing API. Reads pre-parsed fixture
 * data instead of taking a photo and calling a vision model. Swapping in a
 * real client later means implementing `OcrProvider` and changing the one
 * place this is constructed (src/App.vue).
 */
export class MockOcrProvider implements OcrProvider {
  async listScans(): Promise<ScannedReceipt[]> {
    const res = await fetch('/mock/receipts.json')
    if (!res.ok) {
      throw new Error(`Failed to load receipts: ${res.status}`)
    }
    return res.json() as Promise<ScannedReceipt[]>
  }

  async scan(id: string): Promise<ScannedReceipt> {
    // Simulate the latency of an OCR call over the network.
    await new Promise((resolve) => setTimeout(resolve, 700))
    const receipts = await this.listScans()
    const receipt = receipts.find((r) => r.id === id)
    if (!receipt) {
      throw new Error(`No scan found for id "${id}"`)
    }
    return receipt
  }
}
