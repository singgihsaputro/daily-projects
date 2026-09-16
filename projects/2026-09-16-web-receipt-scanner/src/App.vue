<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { MockOcrProvider } from './lib/ocrProvider'
import type { ScannedReceipt } from './lib/types'
import ReceiptResult from './components/ReceiptResult.vue'

const ocr = new MockOcrProvider()

const available = ref<ScannedReceipt[]>([])
const selectedId = ref<string | null>(null)
const scanning = ref(false)
const result = ref<ScannedReceipt | null>(null)
const loadError = ref<string | null>(null)

onMounted(async () => {
  try {
    available.value = await ocr.listScans()
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : 'Failed to load receipts'
  }
})

async function runScan(id: string) {
  selectedId.value = id
  scanning.value = true
  result.value = null
  try {
    result.value = await ocr.scan(id)
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : 'Scan failed'
  } finally {
    scanning.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 px-4 py-10">
    <div class="mx-auto max-w-3xl">
      <header class="mb-8 text-center">
        <h1 class="text-2xl font-bold text-slate-900">Receipt Scanner</h1>
        <p class="mt-1 text-sm text-slate-500">
          Pick a photographed receipt below to run it through OCR.
        </p>
      </header>

      <p v-if="loadError" class="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">
        {{ loadError }}
      </p>

      <div class="mb-8 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <button
          v-for="receipt in available"
          :key="receipt.id"
          type="button"
          class="rounded-xl border-2 border-dashed border-slate-300 bg-white p-4 text-left transition hover:border-slate-400 hover:shadow-sm disabled:cursor-not-allowed disabled:opacity-60"
          :class="selectedId === receipt.id ? 'border-solid border-slate-900' : ''"
          :disabled="scanning"
          @click="runScan(receipt.id)"
        >
          <span class="block text-sm font-medium text-slate-900">{{ receipt.label }}</span>
          <span class="mt-1 block text-xs text-slate-500">{{ receipt.lines.length }} items · photo</span>
        </button>
      </div>

      <div
        v-if="scanning"
        class="flex items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white p-8 text-sm text-slate-500"
      >
        <span class="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-slate-600" />
        Running OCR on the receipt photo…
      </div>

      <ReceiptResult v-else-if="result" :receipt="result" />

      <p v-else class="text-center text-sm text-slate-400">
        No receipt scanned yet — choose one above.
      </p>
    </div>
  </div>
</template>
