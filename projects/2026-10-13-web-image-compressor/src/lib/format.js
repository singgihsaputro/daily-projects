export function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

export function savedPercent(original, compressed) {
  if (original <= 0) return 0
  return Math.round((1 - compressed / original) * 100)
}

export function fitWithin(width, height, maxWidth) {
  if (!maxWidth || width <= maxWidth) return { width, height }
  return { width: maxWidth, height: Math.round((height * maxWidth) / width) }
}
