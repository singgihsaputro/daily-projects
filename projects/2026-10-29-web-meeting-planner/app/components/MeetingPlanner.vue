<script setup lang="ts">
import { cityService, type City } from '~/services/cityService'

const all = ref<City[]>([])
const selectedIds = ref<string[]>(['lon', 'ber', 'nyc'])
const date = ref('2026-11-02')
const picked = ref<number | null>(null)

onMounted(async () => { all.value = await cityService.list() })

const cities = computed(() =>
  selectedIds.value.map(id => all.value.find(c => c.id === id)).filter((c): c is City => !!c))
const available = computed(() => all.value.filter(c => !selectedIds.value.includes(c.id)))
const home = computed(() => cities.value[0])
const slots = computed(() => (home.value ? daySlots(date.value, home.value.zone) : []))
const overlap = computed(() => slots.value.map(t => cities.value.every(c => isWorking(t, c))))
const overlapCount = computed(() => overlap.value.filter(Boolean).length)

function add(e: Event) {
  const el = e.target as HTMLSelectElement
  if (el.value) selectedIds.value.push(el.value)
  el.value = ''
}
function remove(id: string) {
  selectedIds.value = selectedIds.value.filter(x => x !== id)
  picked.value = null
}
function makeHome(id: string) {
  selectedIds.value = [id, ...selectedIds.value.filter(x => x !== id)]
  picked.value = null
}
function bestSlot() {
  const i = overlap.value.indexOf(true)
  picked.value = i === -1 ? null : i
}
function cellClass(t: number, c: City, i: number) {
  return {
    work: isWorking(t, c),
    overlap: overlap.value[i],
    picked: picked.value === i
  }
}
</script>

<template>
  <main class="wrap">
    <header>
      <h1>Meeting Planner</h1>
      <p class="sub">Find an hour that falls inside everyone's working day.</p>
    </header>

    <section class="controls">
      <label>Date <input v-model="date" type="date"></label>
      <label>Add city
        <select :disabled="!available.length" @change="add">
          <option value="">Choose…</option>
          <option v-for="c in available" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </label>
      <button class="primary" :disabled="!overlapCount" @click="bestSlot">Pick earliest overlap</button>
    </section>

    <p class="summary" :class="{ none: !overlapCount }">
      <template v-if="cities.length < 2">Add at least two cities to compare.</template>
      <template v-else-if="overlapCount">{{ overlapCount }} shared working hour{{ overlapCount > 1 ? 's' : '' }} on {{ date }} ({{ home?.name }} day).</template>
      <template v-else>No shared working hours on this day.</template>
    </p>

    <section class="grid" v-if="cities.length">
      <div v-for="c in cities" :key="c.id" class="row">
        <div class="label">
          <strong>{{ c.name }}</strong>
          <span>{{ formatOffset(offsetMinutes(slots[0] ?? 0, c.zone)) }} · {{ c.workStart }}–{{ c.workEnd }}h</span>
          <span class="actions">
            <button v-if="c.id !== home?.id" class="link" @click="makeHome(c.id)">make home</button>
            <button class="link" @click="remove(c.id)">remove</button>
          </span>
        </div>
        <div class="cells">
          <button v-for="(t, i) in slots" :key="t" class="cell" :class="cellClass(t, c, i)"
                  :title="dayLabel(t, c.zone) + ' ' + clock(t, c.zone)" @click="picked = i">
            {{ localParts(t, c.zone).hour }}
          </button>
        </div>
      </div>
    </section>

    <section v-if="picked !== null" class="result">
      <h2>Proposed time</h2>
      <ul>
        <li v-for="c in cities" :key="c.id" :class="{ off: !isWorking(slots[picked], c) }">
          <span>{{ c.name }}</span>
          <strong>{{ dayLabel(slots[picked]!, c.zone) }}, {{ clock(slots[picked]!, c.zone) }}</strong>
          <em v-if="!isWorking(slots[picked]!, c)">outside working hours</em>
        </li>
      </ul>
    </section>
  </main>
</template>
