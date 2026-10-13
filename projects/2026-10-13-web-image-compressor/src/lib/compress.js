import { fitWithin } from './format.js'

function loadImage(blob) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(blob)
    const img = new Image()
    img.onload = () => { URL.revokeObjectURL(url); resolve(img) }
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Could not decode image')) }
    img.src = url
  })
}

// Re-encodes an image blob through a canvas. quality is 0..1 and applies to
// JPEG and WebP; maxWidth of 0 keeps the original dimensions.
export async function compressImage(blob, { format, quality, maxWidth }) {
  const img = await loadImage(blob)
  const naturalWidth = img.naturalWidth || 1600
  const naturalHeight = img.naturalHeight || 1200
  const { width, height } = fitWithin(naturalWidth, naturalHeight, maxWidth)

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.drawImage(img, 0, 0, width, height)

  const out = await new Promise((resolve) => canvas.toBlob(resolve, format, quality))
  if (!out) throw new Error(`This browser cannot encode ${format}`)
  return { blob: out, width, height }
}
