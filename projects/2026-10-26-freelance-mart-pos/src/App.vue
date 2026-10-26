<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { catalog } from './catalog'
import { addToCart, formatRupiah, matches, quickCash, roundCash, setQty, totals } from './cart'
import Receipt from './Receipt.vue'
import type { CartLine, Product, Sale } from './types'

const products = ref<Product[]>([])
const lines = ref<CartLine[]>([])
const query = ref('')
const category = ref('All')
const cash = ref<number | null>(null)
const sale = ref<Sale | null>(null)

onMounted(async () => {
  products.value = await catalog.list()
})

const categories = computed(() => ['All', ...new Set(products.value.map((p) => p.category))])
const visible = computed(() =>
  products.value.filter((p) => (category.value === 'All' || p.category === category.value) && matches(p, query.value)),
)
const sum = computed(() => totals(lines.value))
const due = computed(() => roundCash(sum.value.total))
const change = computed(() => (cash.value === null ? 0 : cash.value - due.value))
const canPay = computed(() => lines.value.length > 0 && cash.value !== null && cash.value >= due.value)
const quick = computed(() => quickCash(sum.value.total))

function scanFirst() {
  if (visible.value.length === 1) {
    lines.value = addToCart(lines.value, visible.value[0])
    query.value = ''
  }
}

async function pay() {
  if (!canPay.value || cash.value === null) return
  sale.value = {
    receiptNo: await catalog.nextReceiptNo(),
    paidAt: new Date(),
    lines: lines.value,
    totals: sum.value,
    tendered: cash.value,
    change: change.value,
  }
}

function printReceipt() {
  window.print()
}

function newSale() {
  sale.value = null
  lines.value = []
  cash.value = null
}
</script>

<template>
  <div class="mx-auto flex h-screen max-w-7xl gap-4 p-4">
    <section class="flex min-w-0 flex-1 flex-col">
      <header class="mb-3 flex items-center gap-3">
        <h1 class="text-xl font-bold text-emerald-700">Mart POS</h1>
        <input
          v-model="query"
          type="search"
          placeholder="Search name or scan barcode, then Enter"
          class="flex-1 rounded-lg border border-slate-300 bg-white px-3 py-2"
          @keydown.enter="scanFirst"
        />
      </header>
      <nav class="mb-3 flex gap-2">
        <button
          v-for="c in categories"
          :key="c"
          class="rounded-full px-3 py-1 text-sm"
          :class="c === category ? 'bg-emerald-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-200'"
          @click="category = c"
        >
          {{ c }}
        </button>
      </nav>
      <div class="grid flex-1 grid-cols-2 content-start gap-3 overflow-y-auto md:grid-cols-3 xl:grid-cols-4">
        <button
          v-for="p in visible"
          :key="p.sku"
          class="rounded-xl bg-white p-3 text-left shadow-sm transition hover:shadow-md active:scale-95"
          @click="lines = addToCart(lines, p)"
        >
          <p class="text-xs text-slate-400">{{ p.category }} · {{ p.sku }}</p>
          <p class="mt-1 font-medium leading-tight">{{ p.name }}</p>
          <p class="mt-2 font-semibold text-emerald-700">{{ formatRupiah(p.price) }}</p>
        </button>
        <p v-if="visible.length === 0" class="col-span-full text-slate-500">No product matches “{{ query }}”.</p>
      </div>
    </section>

    <aside class="flex w-96 flex-col rounded-2xl bg-white p-4 shadow">
      <template v-if="!sale">
        <h2 class="mb-2 font-semibold">Current sale</h2>
        <ul class="flex-1 divide-y divide-slate-100 overflow-y-auto">
          <li v-for="l in lines" :key="l.product.sku" class="flex items-center gap-2 py-2">
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm">{{ l.product.name }}</p>
              <p class="text-xs text-slate-500">{{ formatRupiah(l.product.price) }}</p>
            </div>
            <button class="h-7 w-7 rounded bg-slate-100" aria-label="less" @click="lines = setQty(lines, l.product.sku, l.qty - 1)">−</button>
            <span class="w-6 text-center">{{ l.qty }}</span>
            <button class="h-7 w-7 rounded bg-slate-100" aria-label="more" @click="lines = setQty(lines, l.product.sku, l.qty + 1)">+</button>
            <span class="w-24 text-right text-sm font-medium">{{ formatRupiah(l.qty * l.product.price) }}</span>
          </li>
          <li v-if="lines.length === 0" class="py-8 text-center text-sm text-slate-400">Tap a product to start a sale.</li>
        </ul>
        <dl class="space-y-1 border-t border-slate-200 pt-3 text-sm">
          <div class="flex justify-between"><dt>Subtotal</dt><dd>{{ formatRupiah(sum.subtotal) }}</dd></div>
          <div class="flex justify-between"><dt>PPN 11%</dt><dd>{{ formatRupiah(sum.tax) }}</dd></div>
          <div class="flex justify-between text-lg font-bold"><dt>Total</dt><dd>{{ formatRupiah(due) }}</dd></div>
        </dl>
        <div class="mt-3 flex flex-wrap gap-2">
          <button v-for="n in quick" :key="n" class="rounded-lg bg-slate-100 px-3 py-1 text-sm hover:bg-slate-200" @click="cash = n">
            {{ formatRupiah(n) }}
          </button>
        </div>
        <input
          v-model.number="cash"
          type="number"
          min="0"
          placeholder="Cash received"
          class="mt-2 rounded-lg border border-slate-300 px-3 py-2"
        />
        <p class="mt-2 text-sm" :class="change < 0 ? 'text-red-600' : 'text-slate-600'">
          {{ change < 0 ? 'Short by ' + formatRupiah(-change) : 'Change: ' + formatRupiah(change) }}
        </p>
        <button
          class="mt-3 rounded-lg bg-emerald-600 py-3 font-semibold text-white disabled:bg-slate-300"
          :disabled="!canPay"
          @click="pay"
        >
          Charge {{ formatRupiah(due) }}
        </button>
      </template>

      <template v-else>
        <div class="flex flex-1 justify-center overflow-y-auto"><Receipt :sale="sale" /></div>
        <div class="mt-3 flex gap-2">
          <button class="flex-1 rounded-lg bg-slate-800 py-3 font-semibold text-white" @click="printReceipt">Print receipt</button>
          <button class="flex-1 rounded-lg bg-emerald-600 py-3 font-semibold text-white" @click="newSale">New sale</button>
        </div>
      </template>
    </aside>
  </div>
</template>
