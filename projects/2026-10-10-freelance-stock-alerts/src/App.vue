<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import StatusBadge from './components/StatusBadge.vue'
import { inventory } from './data/repository'
import { matches, rupiah, statusOf, suggestedOrderQty } from './stock'
import type { Filter, Item } from './types'

const items = ref<Item[]>([])
const filter = ref<Filter>('all')
const query = ref('')
const error = ref('')

onMounted(async () => {
  items.value = await inventory.list()
})

const counts = computed(() => ({
  out: items.value.filter((i) => statusOf(i) === 'out').length,
  low: items.value.filter((i) => statusOf(i) === 'low').length,
  value: items.value.reduce((sum, i) => sum + i.stock * i.unitCost, 0),
}))

const visible = computed(() => items.value.filter((i) => matches(i, filter.value, query.value)))

const reorderList = computed(() =>
  items.value
    .filter((i) => statusOf(i) !== 'ok')
    .map((i) => ({ item: i, qty: suggestedOrderQty(i) }))
    .sort((a, b) => a.item.stock - b.item.stock),
)
const reorderCost = computed(() => reorderList.value.reduce((s, r) => s + r.qty * r.item.unitCost, 0))

const tabs: { key: Filter; label: string }[] = [
  { key: 'all', label: 'All' },
  { key: 'attention', label: 'Needs attention' },
  { key: 'low', label: 'Low' },
  { key: 'out', label: 'Out' },
  { key: 'ok', label: 'In stock' },
]

async function change(item: Item, stock: number) {
  try {
    error.value = ''
    const updated = await inventory.setStock(item.sku, stock)
    const idx = items.value.findIndex((i) => i.sku === updated.sku)
    items.value[idx] = updated
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Could not update stock'
  }
}

function receive(item: Item, qty: number) {
  return change(item, item.stock + qty)
}
</script>

<template>
  <main class="mx-auto max-w-6xl p-6">
    <header class="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">Stock Alerts</h1>
        <p class="text-sm text-slate-500">Warung Sentosa · Main warehouse</p>
      </div>
      <dl class="flex gap-3 text-center">
        <div class="rounded-lg bg-red-50 px-4 py-2 ring-1 ring-red-200">
          <dt class="text-xs text-red-700">Out of stock</dt>
          <dd class="text-xl font-semibold text-red-800" data-testid="out-count">{{ counts.out }}</dd>
        </div>
        <div class="rounded-lg bg-amber-50 px-4 py-2 ring-1 ring-amber-200">
          <dt class="text-xs text-amber-700">Low stock</dt>
          <dd class="text-xl font-semibold text-amber-800" data-testid="low-count">{{ counts.low }}</dd>
        </div>
        <div class="rounded-lg bg-white px-4 py-2 ring-1 ring-slate-200">
          <dt class="text-xs text-slate-500">Stock value</dt>
          <dd class="text-xl font-semibold">{{ rupiah(counts.value) }}</dd>
        </div>
      </dl>
    </header>

    <p v-if="error" class="mb-4 rounded bg-red-100 p-3 text-sm text-red-800" role="alert">{{ error }}</p>

    <div class="grid gap-6 lg:grid-cols-[1fr_20rem]">
      <section>
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="rounded-full px-3 py-1 text-sm ring-1"
            :class="filter === t.key ? 'bg-slate-900 text-white ring-slate-900' : 'bg-white ring-slate-300 hover:bg-slate-100'"
            @click="filter = t.key"
          >
            {{ t.label }}
          </button>
          <input
            v-model="query"
            type="search"
            placeholder="Search SKU, name, category"
            class="ml-auto w-56 rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm"
          />
        </div>

        <div class="overflow-hidden rounded-lg bg-white ring-1 ring-slate-200">
          <table class="w-full text-left text-sm">
            <thead class="bg-slate-100 text-xs uppercase text-slate-500">
              <tr>
                <th class="px-3 py-2">Item</th>
                <th class="px-3 py-2">Status</th>
                <th class="px-3 py-2 text-right">Stock</th>
                <th class="px-3 py-2 text-right">Reorder at</th>
                <th class="px-3 py-2 text-right">Adjust</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in visible" :key="item.sku" class="border-t border-slate-100">
                <td class="px-3 py-2">
                  <div class="font-medium">{{ item.name }}</div>
                  <div class="text-xs text-slate-500">{{ item.sku }} · {{ item.category }}</div>
                </td>
                <td class="px-3 py-2"><StatusBadge :status="statusOf(item)" /></td>
                <td class="px-3 py-2 text-right font-mono tabular-nums">{{ item.stock }}</td>
                <td class="px-3 py-2 text-right font-mono tabular-nums text-slate-500">{{ item.reorderLevel }}</td>
                <td class="px-3 py-2 text-right">
                  <div class="inline-flex gap-1">
                    <button
                      class="rounded border border-slate-300 px-2 py-0.5 hover:bg-slate-100 disabled:opacity-40"
                      :disabled="item.stock === 0"
                      :aria-label="`Sell one ${item.name}`"
                      @click="change(item, item.stock - 1)"
                    >
                      −1
                    </button>
                    <button
                      class="rounded border border-slate-300 px-2 py-0.5 hover:bg-slate-100"
                      :aria-label="`Add one ${item.name}`"
                      @click="receive(item, 1)"
                    >
                      +1
                    </button>
                    <button
                      class="rounded bg-slate-900 px-2 py-0.5 text-white hover:bg-slate-700"
                      @click="receive(item, item.reorderQty)"
                    >
                      Receive {{ item.reorderQty }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="!visible.length">
                <td colspan="5" class="px-3 py-8 text-center text-slate-500">No items match.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <aside class="h-fit rounded-lg bg-white p-4 ring-1 ring-slate-200">
        <h2 class="font-semibold">Suggested reorder</h2>
        <p v-if="!reorderList.length" class="mt-2 text-sm text-slate-500">Everything is above its reorder level.</p>
        <ul v-else class="mt-3 space-y-3 text-sm">
          <li v-for="r in reorderList" :key="r.item.sku">
            <div class="flex justify-between gap-2">
              <span class="font-medium">{{ r.item.name }}</span>
              <span class="font-mono">×{{ r.qty }}</span>
            </div>
            <div class="text-xs text-slate-500">{{ r.item.supplier }} · {{ rupiah(r.qty * r.item.unitCost) }}</div>
          </li>
        </ul>
        <p v-if="reorderList.length" class="mt-4 flex justify-between border-t border-slate-200 pt-3 text-sm font-semibold">
          <span>Estimated total</span><span>{{ rupiah(reorderCost) }}</span>
        </p>
      </aside>
    </div>
  </main>
</template>
