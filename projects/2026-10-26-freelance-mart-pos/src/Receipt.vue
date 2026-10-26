<script setup lang="ts">
import { formatRupiah, roundCash } from './cart'
import type { Sale } from './types'

defineProps<{ sale: Sale }>()
</script>

<template>
  <div id="receipt" class="w-80 bg-white p-5 font-mono text-xs shadow-lg">
    <div class="text-center">
      <p class="text-sm font-bold">WARUNG MAJU MART</p>
      <p>Jl. Melati No. 12, Bandung</p>
      <p>{{ sale.paidAt.toLocaleString('en-GB') }}</p>
      <p>{{ sale.receiptNo }}</p>
    </div>
    <hr class="my-2 border-dashed border-slate-400" />
    <div v-for="l in sale.lines" :key="l.product.sku" class="mb-1">
      <p>{{ l.product.name }}</p>
      <p class="flex justify-between">
        <span>{{ l.qty }} x {{ formatRupiah(l.product.price) }}</span>
        <span>{{ formatRupiah(l.qty * l.product.price) }}</span>
      </p>
    </div>
    <hr class="my-2 border-dashed border-slate-400" />
    <p class="flex justify-between"><span>Subtotal</span><span>{{ formatRupiah(sale.totals.subtotal) }}</span></p>
    <p class="flex justify-between"><span>PPN 11%</span><span>{{ formatRupiah(sale.totals.tax) }}</span></p>
    <p class="flex justify-between"><span>Rounding</span><span>{{ formatRupiah(roundCash(sale.totals.total) - sale.totals.total) }}</span></p>
    <p class="flex justify-between font-bold"><span>Total</span><span>{{ formatRupiah(roundCash(sale.totals.total)) }}</span></p>
    <p class="flex justify-between"><span>Cash</span><span>{{ formatRupiah(sale.tendered) }}</span></p>
    <p class="flex justify-between"><span>Change</span><span>{{ formatRupiah(sale.change) }}</span></p>
    <p class="mt-3 text-center">Terima kasih!</p>
  </div>
</template>
