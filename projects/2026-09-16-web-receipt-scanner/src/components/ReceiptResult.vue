<script setup lang="ts">
import { computed } from 'vue'
import type { ScannedReceipt } from '../lib/types'

const props = defineProps<{ receipt: ScannedReceipt }>()

const subtotal = computed(() =>
  props.receipt.lines.reduce((sum, line) => sum + line.price, 0),
)
const tax = computed(() => subtotal.value * props.receipt.taxRate)
const total = computed(() => subtotal.value + tax.value)
const lowConfidenceCount = computed(
  () => props.receipt.lines.filter((line) => line.confidence < 0.75).length,
)

function formatMoney(value: number): string {
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' })
}
</script>

<template>
  <div class="rounded-xl border border-slate-200 bg-white p-5">
    <div class="flex items-baseline justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">{{ receipt.merchant }}</h2>
        <p class="text-sm text-slate-500">{{ receipt.date }}</p>
      </div>
      <span
        v-if="lowConfidenceCount > 0"
        class="rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800"
      >
        {{ lowConfidenceCount }} line{{ lowConfidenceCount > 1 ? 's' : '' }} need review
      </span>
    </div>

    <ul class="mt-4 divide-y divide-slate-100">
      <li
        v-for="(line, index) in receipt.lines"
        :key="index"
        class="flex items-center justify-between gap-3 py-2"
      >
        <div class="flex min-w-0 items-center gap-2">
          <span
            class="h-2 w-2 shrink-0 rounded-full"
            :class="line.confidence < 0.75 ? 'bg-amber-500' : 'bg-emerald-500'"
            :title="`OCR confidence ${(line.confidence * 100).toFixed(0)}%`"
          />
          <input
            v-model="line.name"
            class="min-w-0 flex-1 rounded border border-transparent bg-transparent px-1 py-0.5 text-sm text-slate-700 focus:border-slate-300 focus:bg-slate-50 focus:outline-none"
          />
        </div>
        <div class="flex shrink-0 items-center gap-1 text-sm text-slate-700">
          <span>$</span>
          <input
            v-model.number="line.price"
            type="number"
            step="0.01"
            min="0"
            class="w-20 rounded border border-transparent bg-transparent px-1 py-0.5 text-right focus:border-slate-300 focus:bg-slate-50 focus:outline-none"
          />
        </div>
      </li>
    </ul>

    <dl class="mt-4 space-y-1 border-t border-slate-100 pt-3 text-sm">
      <div class="flex justify-between text-slate-500">
        <dt>Subtotal</dt>
        <dd>{{ formatMoney(subtotal) }}</dd>
      </div>
      <div class="flex justify-between text-slate-500">
        <dt>Tax ({{ (receipt.taxRate * 100).toFixed(1) }}%)</dt>
        <dd>{{ formatMoney(tax) }}</dd>
      </div>
      <div class="flex justify-between text-base font-semibold text-slate-900">
        <dt>Total</dt>
        <dd>{{ formatMoney(total) }}</dd>
      </div>
    </dl>
    <p class="mt-3 text-xs text-slate-400">
      Low-confidence lines are flagged for review — edit the name or price above to correct them.
    </p>
  </div>
</template>
