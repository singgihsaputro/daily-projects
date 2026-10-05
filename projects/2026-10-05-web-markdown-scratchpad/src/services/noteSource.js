import seed from '../../mock/notes.json'

// The only file that knows where notes come from. A real HTTP client only has
// to export the same two async functions.
const STORAGE_KEY = 'scratchpad.notes'

export async function loadNotes() {
  const saved = localStorage.getItem(STORAGE_KEY)
  return saved ? JSON.parse(saved) : structuredClone(seed)
}

export async function saveNotes(notes) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(notes))
}
