// Sums ingredient quantities across the planned recipes, merging same name+unit.
export function buildShoppingList(recipes) {
  const totals = new Map()
  for (const recipe of recipes) {
    for (const [name, qty, unit] of recipe.ingredients) {
      const key = `${name}|${unit}`
      const entry = totals.get(key) ?? { name, unit, qty: 0 }
      entry.qty += qty
      totals.set(key, entry)
    }
  }
  return [...totals.values()].sort((a, b) => a.name.localeCompare(b.name))
}
