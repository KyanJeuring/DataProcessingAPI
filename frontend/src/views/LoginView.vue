<script setup>
import "../style/loginview.css";
import { ref } from "vue";

const email = ref(sessionStorage.getItem("pendingEmail") || "");
const password = ref("");
const error = ref("");
const token = ref("");
const showToken = ref(false);

const base = (import.meta.env.VITE_BACKEND_URL || "/api").replace(/\/$/, "");

async function login() {
  error.value = "";
  token.value = "";

  try {
    const res = await fetch(`${base}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email.value,
        password: password.value,
      }),
    });

    if (!res.ok) throw new Error("Invalid email or password");

    const data = await res.json();
    if (data.token) {
      localStorage.setItem("token", data.token);
      token.value = data.token;
      showToken.value = true;
    }
  } catch (e) {
    error.value = e.message;
  }
}

function copyToken() {
  navigator.clipboard.writeText(token.value);
}

function goToDashboard() {
  window.location.href = "/dashboard";
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2>FleetMaster Login</h2>
      <p class="login-subtitle">Access your dashboard</p>

      <form v-if="!showToken" @submit.prevent="login">
        <input v-model="email" type="email" placeholder="Email" required/>
        <input v-model="password" type="password" placeholder="Password" required/>
        <button type="submit">Login</button>
      </form>

      <div v-if="showToken" class="token-display">
        <h3>Login Successful! ✓</h3>
        <p class="token-label">Your JWT Token (for Swagger API testing):</p>
        <div class="token-box">
          <code class="token-text">{{ token }}</code>
          <button class="copy-btn" @click="copyToken" title="Copy to clipboard">📋 Copy</button>
        </div>
        <p class="token-hint">Paste this token in Swagger's Authorize button to test protected endpoints</p>
        <button class="dashboard-btn" @click="goToDashboard">Go to Dashboard</button>
      </div>

      <div class="login-footer">
        <p v-if="!showToken">Don't have an account?<a href="/signup">Sign up</a></p>
      </div>
      <p v-if="error" class="login-error">{{ error }}</p>
    </div>
  </div>
</template>
