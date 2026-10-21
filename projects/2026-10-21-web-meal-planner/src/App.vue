<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { DAYS, usePlanStore } from './stores/plan.js'

const plan = usePlanStore()
const { recipes, assigned, totalKcal, shoppingList, plannedRecipes } = storeToRefs(plan)

onMounted(plan.load)

const kcalFor = (day) => plan.byId(assigned.value[day])?.kcal ?? 0
</script>

<template>
  <main class="mx-auto max-w-5xl p-6 text-stone-800">
    <header class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 class="text-2xl font-bold">Weekly Meal Planner</h1>
        <p class="text-sm text-stone-500">Pick a dinner for each day; the shopping list builds itself.</p>
      </div>
      <div class="flex gap-2">
        <button class="rounded bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700" @click="plan.shuffle">Surprise me</button>
        <button class="rounded border border-stone-300 bg-white px-3 py-1.5 text-sm hover:bg-stone-50" @click="plan.clear">Clear</button>
      </div>
    </header>

    <div class="mt-6 grid gap-6 md:grid-cols-5">
      <section class="md:col-span-3">
        <ul class="space-y-2">
          <li v-for="day in DAYS" :key="day" class="flex items-center gap-3 rounded-lg bg-white p-3 shadow-sm">
            <span class="w-10 font-semibold text-emerald-700">{{ day }}</span>
            <select
              class="min-w-0 flex-1 rounded border border-stone-300 px-2 py-1.5 text-sm"
              :aria-label="`Dinner on ${day}`"
              :value="assigned[day] ?? ''"
              @change="plan.assign(day, $event.target.value)"
            >
              <option value="">— nothing planned —</option>
              <option v-for="r in recipes" :key="r.id" :value="r.id">{{ r.name }}</option>
            </select>
            <span class="w-20 text-right text-sm tabular-nums text-stone-500">{{ kcalFor(day) ? kcalFor(day) + ' kcal' : '' }}</span>
          </li>
        </ul>
        <p class="mt-3 text-sm text-stone-600">
          {{ plannedRecipes.length }} of 7 days planned ·
          <strong>{{ totalKcal }} kcal</strong> total
          <template v-if="plannedRecipes.length">· avg {{ Math.round(totalKcal / plannedRecipes.length) }} kcal/meal</template>
        </p>
      </section>

      <aside class="md:col-span-2">
        <div class="rounded-lg bg-white p-4 shadow-sm">
          <h2 class="font-semibold">Shopping list</h2>
          <p v-if="!shoppingList.length" class="mt-2 text-sm text-stone-400">Plan a meal to see what to buy.</p>
          <ul v-else class="mt-2 divide-y divide-stone-100 text-sm">
            <li v-for="item in shoppingList" :key="item.name + item.unit" class="flex justify-between py-1.5">
              <span class="capitalize">{{ item.name }}</span>
              <span class="tabular-nums text-stone-500">{{ item.qty }} {{ item.unit }}</span>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </main>
</template>
