import { defineStore } from 'pinia'
import { loadNotes, saveNotes } from '../services/noteSource'

export const useNotesStore = defineStore('notes', {
  state: () => ({ notes: [], activeId: null, query: '', ready: false }),
  getters: {
    active: (s) => s.notes.find((n) => n.id === s.activeId) ?? null,
    filtered: (s) => {
      const q = s.query.trim().toLowerCase()
      return [...s.notes]
        .filter((n) => !q || n.title.toLowerCase().includes(q) || n.body.toLowerCase().includes(q))
        .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    },
  },
  actions: {
    async init() {
      this.notes = await loadNotes()
      this.activeId = this.filtered[0]?.id ?? null
      this.ready = true
    },
    select(id) {
      this.activeId = id
    },
    create() {
      const note = { id: `n${Date.now()}`, title: 'Untitled', body: '# Untitled\n\n', updatedAt: new Date().toISOString() }
      this.notes.push(note)
      this.activeId = note.id
      return saveNotes(this.notes)
    },
    update(body) {
      const note = this.active
      if (!note) return
      note.body = body
      const first = body.split('\n').find((l) => l.trim() !== '') ?? ''
      note.title = first.replace(/^#+\s*/, '').trim() || 'Untitled'
      note.updatedAt = new Date().toISOString()
      return saveNotes(this.notes)
    },
    remove(id) {
      this.notes = this.notes.filter((n) => n.id !== id)
      if (this.activeId === id) this.activeId = this.filtered[0]?.id ?? null
      return saveNotes(this.notes)
    },
  },
})
