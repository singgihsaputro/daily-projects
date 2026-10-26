import products from '../mock/products.json'
import type { Product } from './types'

export interface CatalogSource {
  list(): Promise<Product[]>
  nextReceiptNo(): Promise<string>
}

// Mock implementation. A real API client replaces only this export.
class MockCatalog implements CatalogSource {
  private counter = 41

  async list(): Promise<Product[]> {
    return products
  }

  async nextReceiptNo(): Promise<string> {
    this.counter += 1
    return `INV-${String(this.counter).padStart(5, '0')}`
  }
}

export const catalog: CatalogSource = new MockCatalog()
