import test from 'node:test'
import assert from 'node:assert/strict'
import { addToCart, formatRupiah, quickCash, roundCash, setQty, totals } from './cart.ts'

const noodle = { sku: '1', name: 'Noodle', price: 3500, category: 'Food' }
const water = { sku: '2', name: 'Water', price: 3500, category: 'Drinks' }

test('adding the same product increments quantity', () => {
  const lines = addToCart(addToCart([], noodle), noodle)
  assert.equal(lines.length, 1)
  assert.equal(lines[0].qty, 2)
})

test('setting quantity to zero removes the line', () => {
  const lines = setQty(addToCart(addToCart([], noodle), water), '1', 0)
  assert.deepEqual(lines.map((l) => l.product.sku), ['2'])
})

test('totals include 11% PPN', () => {
  const t = totals(addToCart(addToCart([], noodle), water))
  assert.deepEqual(t, { subtotal: 7000, tax: 770, total: 7770 })
})

test('formatRupiah groups thousands with dots', () => {
  assert.equal(formatRupiah(1234567), 'Rp 1.234.567')
  assert.equal(formatRupiah(0), 'Rp 0')
})

test('cash rounding and quick-cash buttons', () => {
  assert.equal(roundCash(7770), 7800)
  assert.deepEqual(quickCash(7770), [7800, 20000, 50000, 100000])
})
