import { test } from 'node:test'
import assert from 'node:assert/strict'
import { formatBytes, savedPercent, fitWithin } from './format.js'

test('formatBytes picks a sensible unit', () => {
  assert.equal(formatBytes(512), '512 B')
  assert.equal(formatBytes(2048), '2.0 KB')
  assert.equal(formatBytes(3 * 1024 * 1024), '3.00 MB')
})

test('savedPercent', () => {
  assert.equal(savedPercent(1000, 250), 75)
  assert.equal(savedPercent(0, 0), 0)
  assert.equal(savedPercent(100, 120), -20)
})

test('fitWithin keeps aspect ratio and never upscales', () => {
  assert.deepEqual(fitWithin(1600, 1200, 800), { width: 800, height: 600 })
  assert.deepEqual(fitWithin(400, 300, 800), { width: 400, height: 300 })
  assert.deepEqual(fitWithin(400, 300, 0), { width: 400, height: 300 })
})
