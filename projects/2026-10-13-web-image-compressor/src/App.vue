<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import imageSource from './services/imageSource.js'
import { compressImage } from './lib/compress.js'
import { formatBytes, savedPercent } from './lib/format.js'

const formats = [
  { value: 'image/jpeg', label: 'JPEG', ext: 'jpg' },
  { value: 'image/webp', label: 'WebP', ext: 'webp' },
  { value: 'image/png', label: 'PNG (lossless)', ext: 'png' },
]

const samples = ref([])
const selected = ref(null)
const original = ref(null)
const originalUrl = ref('')
const result = ref(null)
const resultUrl = ref('')
const error = ref('')
const busy = ref(false)

const format = ref('image/jpeg')
const quality = ref(0.7)
const maxWidth = ref(1200)

const formatInfo = computed(() => formats.find((f) => f.value === format.value))
const saved = computed(() => (result.value ? savedPercent(original.value.size, result.value.blob.size) : 0))
const downloadName = computed(() => {
  const base = selected.value.name.replace(/\.[^.]+$/, '')
  return `${base}-compressed.${formatInfo.value.ext}`
})

function setOriginal(item, blob) {
  selected.value = item
  original.value = blob
  if (originalUrl.value) URL.revokeObjectURL(originalUrl.value)
  originalUrl.value = URL.createObjectURL(blob)
}

async function pickSample(sample) {
  error.value = ''
  try {
    setOriginal(sample, await imageSource.fetchBlob(sample))
  } catch (e) {
    error.value = e.message
  }
}

function onFile(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    error.value = 'Please choose an image file.'
    return
  }
  error.value = ''
  setOriginal({ id: 'upload', name: file.name, caption: 'Your upload' }, file)
}

let run = 0
async function recompress() {
  if (!original.value) return
  const ticket = ++run
  busy.value = true
  try {
    const out = await compressImage(original.value, {
      format: format.value,
      quality: quality.value,
      maxWidth: maxWidth.value,
    })
    if (ticket !== run) return
    result.value = out
    if (resultUrl.value) URL.revokeObjectURL(resultUrl.value)
    resultUrl.value = URL.createObjectURL(out.blob)
    error.value = ''
  } catch (e) {
    if (ticket === run) error.value = e.message
  } finally {
    if (ticket === run) busy.value = false
  }
}

watch([original, format, quality, maxWidth], recompress)

onMounted(async () => {
  samples.value = await imageSource.list()
  await pickSample(samples.value[0])
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <header class="border-b border-slate-200 bg-white">
      <div class="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <h1 class="text-xl font-semibold">Image Compressor</h1>
        <p class="text-sm text-slate-500">Runs entirely in your browser — nothing is uploaded.</p>
      </div>
    </header>

    <main class="mx-auto grid max-w-5xl gap-6 px-6 py-6 md:grid-cols-[18rem_1fr]">
      <aside class="space-y-5">
        <section class="rounded-xl border border-slate-200 bg-white p-4">
          <h2 class="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Source</h2>
          <ul class="space-y-2">
            <li v-for="s in samples" :key="s.id">
              <button
                class="w-full rounded-lg border px-3 py-2 text-left text-sm transition"
                :class="selected?.id === s.id ? 'border-indigo-500 bg-indigo-50' : 'border-slate-200 hover:bg-slate-50'"
                @click="pickSample(s)"
              >
                <span class="block font-medium">{{ s.name }}</span>
                <span class="block text-xs text-slate-500">{{ s.caption }}</span>
              </button>
            </li>
          </ul>
          <label class="mt-3 block text-sm">
            <span class="mb-1 block text-slate-500">…or choose your own</span>
            <input type="file" accept="image/*" class="block w-full text-sm" @change="onFile" />
          </label>
        </section>

        <section class="rounded-xl border border-slate-200 bg-white p-4 space-y-4">
          <h2 class="text-sm font-semibold uppercase tracking-wide text-slate-500">Settings</h2>
          <label class="block text-sm">
            Format
            <select v-model="format" class="mt-1 w-full rounded-lg border border-slate-300 px-2 py-1.5">
              <option v-for="f in formats" :key="f.value" :value="f.value">{{ f.label }}</option>
            </select>
          </label>
          <label class="block text-sm">
            Quality: {{ Math.round(quality * 100) }}%
            <input
              v-model.number="quality" type="range" min="0.1" max="1" step="0.05"
              :disabled="format === 'image/png'" class="mt-1 w-full"
            />
          </label>
          <label class="block text-sm">
            Max width
            <select v-model.number="maxWidth" class="mt-1 w-full rounded-lg border border-slate-300 px-2 py-1.5">
              <option :value="0">Original size</option>
              <option :value="1600">1600 px</option>
              <option :value="1200">1200 px</option>
              <option :value="800">800 px</option>
              <option :value="400">400 px</option>
            </select>
          </label>
        </section>
      </aside>

      <section class="space-y-4">
        <p v-if="error" role="alert" class="rounded-lg bg-red-50 px-4 py-2 text-sm text-red-700">{{ error }}</p>

        <div v-if="result" class="grid grid-cols-3 gap-3 text-center">
          <div class="rounded-xl border border-slate-200 bg-white p-3">
            <div class="text-xs uppercase text-slate-500">Original</div>
            <div class="text-lg font-semibold" data-testid="original-size">{{ formatBytes(original.size) }}</div>
          </div>
          <div class="rounded-xl border border-slate-200 bg-white p-3">
            <div class="text-xs uppercase text-slate-500">Compressed</div>
            <div class="text-lg font-semibold" data-testid="compressed-size">{{ formatBytes(result.blob.size) }}</div>
            <div class="text-xs text-slate-500">{{ result.width }} × {{ result.height }}</div>
          </div>
          <div class="rounded-xl p-3" :class="saved >= 0 ? 'bg-emerald-50 text-emerald-800' : 'bg-amber-50 text-amber-800'">
            <div class="text-xs uppercase">{{ saved >= 0 ? 'Saved' : 'Larger by' }}</div>
            <div class="text-lg font-semibold" data-testid="saved">{{ Math.abs(saved) }}%</div>
          </div>
        </div>

        <div class="grid gap-4 sm:grid-cols-2">
          <figure class="rounded-xl border border-slate-200 bg-white p-3">
            <figcaption class="mb-2 text-sm font-medium">Before</figcaption>
            <img v-if="originalUrl" :src="originalUrl" alt="Original" class="w-full rounded-lg" />
          </figure>
          <figure class="rounded-xl border border-slate-200 bg-white p-3" :class="{ 'opacity-60': busy }">
            <figcaption class="mb-2 text-sm font-medium">After</figcaption>
            <img v-if="resultUrl" :src="resultUrl" alt="Compressed" class="w-full rounded-lg" />
          </figure>
        </div>

        <a
          v-if="result"
          :href="resultUrl" :download="downloadName"
          class="inline-block rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
        >
          Download {{ downloadName }}
        </a>
      </section>
    </main>
  </div>
</template>
