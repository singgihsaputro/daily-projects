// The app only talks to this interface: list() -> [{ id, name, url, caption }]
// and fetchBlob(image) -> Blob. To use a real backend, replace this file's
// default export with a client that returns the same shapes.
import images from '../../mock/images.json'

export const mockImageSource = {
  async list() {
    return images
  },
  async fetchBlob(image) {
    const res = await fetch(image.url)
    if (!res.ok) throw new Error(`Could not load ${image.name}`)
    return res.blob()
  },
}

export default mockImageSource
