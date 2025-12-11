<script setup>
import { ref } from 'vue'

const message = ref('')
const name = ref('')
const loading = ref(false)
const helloStatus = ref('')

// Company form state
const companyName = ref('')
const companyLicense = ref('')
const companyDiscount = ref(false)
const companySubmitting = ref(false)
const companies = ref([])
const companyStatus = ref('')
const listStatus = ref('')

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
    helloStatus.value = `${res.status} ${res.statusText}`
    console.log('[HELLO]', res.status, res.statusText)
    if (!res.ok) throw new Error(`status=${res.status}`)
    message.value = await res.text()
  } catch (err) {
    console.error(err)
    message.value = 'Error contacting backend'
    if (!helloStatus.value) helloStatus.value = 'Request failed'
  } finally {
    loading.value = false
  }
}

async function loadCompanies() {
  try {
    const base = getBackendBase().replace(/\/$/, '')
    const res = await fetch(`${base}/companies`)
    listStatus.value = `${res.status} ${res.statusText}`
    console.log('[COMPANIES LIST]', res.status, res.statusText)
    if (res.ok) {
      companies.value = await res.json()
    }
  } catch (e) {
    // ignore for initial load
    if (!listStatus.value) listStatus.value = 'Request failed'
  }
}

async function submitCompany(e) {
  e.preventDefault()
  if (!companyName.value.trim()) return
  companySubmitting.value = true
  try {
    const base = getBackendBase().replace(/\/$/, '')
    const payload = {
      name: companyName.value.trim(),
      license: companyLicense.value ? Number(companyLicense.value) : null,
      discountReceived: !!companyDiscount.value,
    }
    const res = await fetch(`${base}/companies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    companyStatus.value = `${res.status} ${res.statusText}`
    console.log('[COMPANIES CREATE]', res.status, res.statusText)
    if (!res.ok) throw new Error(`status=${res.status}`)
    const created = await res.json()
    companies.value.unshift(created)
    companyName.value = ''
    companyLicense.value = ''
    companyDiscount.value = false
  } catch (err) {
    alert('Failed to create company')
    console.error(err)
    if (!companyStatus.value) companyStatus.value = 'Request failed'
  } finally {
    companySubmitting.value = false
  }
}

loadCompanies()
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
      <div v-if="helloStatus"><em>Status: {{ helloStatus }}</em></div>
    </div>
  </section>

  <section>
    <h1>Add Company</h1>
    <form @submit="submitCompany" style="display:grid; gap:0.5rem; max-width: 420px;">
      <label>
        Name
        <input v-model="companyName" placeholder="e.g., Acme Logistics" required />
      </label>
      <label>
        License (number)
        <input v-model="companyLicense" inputmode="numeric" pattern="[0-9]*" placeholder="optional" />
      </label>
      <label style="display:flex; align-items:center; gap:0.5rem;">
        <input type="checkbox" v-model="companyDiscount" />
        Discount received
      </label>
      <button type="submit" :disabled="companySubmitting">Create company</button>
    </form>

    <div v-if="companyStatus" style="margin-top:0.5rem;">
      <em>Last create status: {{ companyStatus }}</em>
    </div>

    <div v-if="companies.length" style="margin-top:1rem;">
      <h3>Companies</h3>
      <div v-if="listStatus"><em>List status: {{ listStatus }}</em></div>
      <ul>
        <li v-for="c in companies" :key="c.id">
          #{{ c.id }} — {{ c.name }}
          <span v-if="c.license != null">(license: {{ c.license }})</span>
          <span>— discount: {{ c.discountReceived ? 'yes' : 'no' }}</span>
        </li>
      </ul>
    </div>
  </section>
</template>
