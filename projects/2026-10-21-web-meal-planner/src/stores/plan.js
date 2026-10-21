import { defineStore } from 'pinia'
import { buildShoppingList } from '../lib/shopping.js'
import { recipeSource } from '../services/recipeSource.js'

export const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

export const usePlanStore = defineStore('plan', {
  state: () => ({
    recipes: [],
    // day -> recipe id (or null)
    assigned: Object.fromEntries(DAYS.map((d) => [d, null])),
  }),
  getters: {
    byId: (s) => (id) => s.recipes.find((r) => r.id === id) ?? null,
    plannedRecipes(s) {
      return DAYS.map((d) => this.byId(s.assigned[d])).filter(Boolean)
    },
    totalKcal() {
      return this.plannedRecipes.reduce((sum, r) => sum + r.kcal, 0)
    },
    shoppingList() {
      return buildShoppingList(this.plannedRecipes)
    },
  },
  actions: {
    async load() {
      this.recipes = await recipeSource.list()
    },
    assign(day, id) {
      this.assigned[day] = id || null
    },
    clear() {
      for (const d of DAYS) this.assigned[d] = null
    },
    shuffle() {
      const pool = [...this.recipes]
      for (const d of DAYS) {
        if (!pool.length) pool.push(...this.recipes)
        const i = Math.floor(Math.random() * pool.length)
        this.assigned[d] = pool.splice(i, 1)[0].id
      }
    },
  },
})
