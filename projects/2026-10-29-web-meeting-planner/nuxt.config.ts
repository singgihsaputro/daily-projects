export default defineNuxtConfig({
  ssr: false,
  compatibilityDate: '2025-07-15',
  devtools: { enabled: false },
  telemetry: false,
  css: ['~/assets/main.css'],
  app: { head: { title: 'Meeting Planner' } }
})
