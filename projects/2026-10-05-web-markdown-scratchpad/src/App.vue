<script setup>
import { computed, onMounted } from 'vue'
import { useNotesStore } from './stores/notes'
import { renderMarkdown, countWords } from './lib/markdown'

const store = useNotesStore()
onMounted(() => store.init())

const html = computed(() => (store.active ? renderMarkdown(store.active.body) : ''))
const words = computed(() => (store.active ? countWords(store.active.body) : 0))
const fmt = (iso) => new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', timeZone: 'UTC' })
</script>

<template>
  <div class="app">
    <aside class="sidebar">
      <header>
        <h1>Scratchpad</h1>
        <button class="primary" @click="store.create()">+ New</button>
      </header>
      <input v-model="store.query" class="search" type="search" placeholder="Search notes…" />
      <ul class="notes">
        <li v-for="n in store.filtered" :key="n.id" :class="{ active: n.id === store.activeId }" @click="store.select(n.id)">
          <strong>{{ n.title }}</strong>
          <span>{{ fmt(n.updatedAt) }}</span>
        </li>
        <li v-if="store.ready && !store.filtered.length" class="empty">No notes match.</li>
      </ul>
    </aside>

    <main v-if="store.active" class="editor">
      <section class="pane">
        <div class="pane-head">
          <span>Markdown</span>
          <span>{{ words }} words</span>
        </div>
        <textarea :value="store.active.body" spellcheck="false" @input="store.update($event.target.value)" />
      </section>
      <section class="pane">
        <div class="pane-head">
          <span>Preview</span>
          <button class="danger" @click="store.remove(store.active.id)">Delete</button>
        </div>
        <article class="preview" v-html="html" />
      </section>
    </main>
    <main v-else class="editor blank">Select a note or create a new one.</main>
  </div>
</template>
