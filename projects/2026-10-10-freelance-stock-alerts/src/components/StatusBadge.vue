<script setup lang="ts">
import { computed } from 'vue'
import type { StockStatus } from '../types'

const props = defineProps<{ status: StockStatus }>()

const look = computed(() => ({
  out: { label: 'Out of stock', cls: 'bg-red-100 text-red-800 ring-red-200' },
  low: { label: 'Low stock', cls: 'bg-amber-100 text-amber-800 ring-amber-200' },
  ok: { label: 'In stock', cls: 'bg-emerald-100 text-emerald-800 ring-emerald-200' },
})[props.status])
</script>

<template>
  <span class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ring-1" :class="look.cls">
    {{ look.label }}
  </span>
</template>
