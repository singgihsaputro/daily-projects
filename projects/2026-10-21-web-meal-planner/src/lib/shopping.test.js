import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { buildShoppingList } from './shopping.js'

const recipes = JSON.parse(readFileSync(new URL('../../mock/recipes.json', import.meta.url)))

test('merges same ingredient and unit across recipes', () => {
  const list = buildShoppingList([recipes[2], recipes[3]])
  const garlic = list.find((i) => i.name === 'garlic')
  assert.equal(garlic.qty, 4)
  assert.equal(garlic.unit, 'cloves')
})

test('list is sorted by name and empty plan gives empty list', () => {
  assert.deepEqual(buildShoppingList([]), [])
  const names = buildShoppingList(recipes).map((i) => i.name)
  assert.deepEqual(names, [...names].sort((a, b) => a.localeCompare(b)))
})
