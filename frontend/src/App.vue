<script setup>
import { ref } from 'vue'

const message = ref('')
const name = ref('')
const loading = ref(false)

function getBackendBase() {
  // Vite-proxied path by default (set in docker-compose to `/api`)
  return import.meta.env.VITE_BACKEND_URL || '/api'
}

async function submit(e) {
  e.preventDefault()
  message.value = ''
  loading.value = true
  try {
    const base = getBackendBase().replace(/\/$/, '')
    const url = `${base}/hello${name.value ? `?name=${encodeURIComponent(name.value)}` : ''}`
    const res = await fetch(url)
    if (!res.ok) throw new Error(`status=${res.status}`)
    message.value = await res.text()
  } catch (err) {
    console.error(err)
    message.value = 'Error contacting backend'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section>
    <h1>Data Processing API</h1>
    <p>This is a simple frontend to demonstrate communication with the backend API.</p>
    <hr>
    <p>The front-end runs on Vue.js + Vite.</p>
    <p>The backend runs on Java + Spring Boot.</p>
  </section>
  <section>
    <h1>Say Hello</h1>
    <form @submit="submit">
      <label for="name">Your name:</label>
      <input id="name" v-model="name" placeholder="Enter your name" />
      <button type="submit" :disabled="loading">Send</button>
    </form>

    <div>
      <strong>Response:</strong>
      <div>{{ message }}</div>
    </div>
  </section>
</template>
