import cities from '~~/mock/cities.json'

export interface City {
  id: string
  name: string
  zone: string
  workStart: number
  workEnd: number
}

export interface CityService {
  list(): Promise<City[]>
}

// Swap this file's export for an HTTP client to use a real directory of cities.
export const cityService: CityService = {
  async list() {
    return cities as City[]
  }
}
