<script setup>
import "../style/loginview.css";
import { ref } from "vue";

const email = ref(sessionStorage.getItem("pendingEmail") || "");
const password = ref("");
const error = ref("");

const base = (import.meta.env.VITE_BACKEND_URL || "/api").replace(/\/$/, "");

async function login() {
  error.value = "";

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
      window.location.href = "/dashboard";
    }
  } catch (e) {
    error.value = e.message;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2>FleetMaster Login</h2>
      <p class="login-subtitle">Access your dashboard</p>

      <form @submit.prevent="login">
        <input v-model="email" type="email" placeholder="Email" required/>
        <input v-model="password" type="password" placeholder="Password" required/>
        <button type="submit">Login</button>
      </form>

      <div class="login-footer">
        <p>Don't have an account?<a href="/signup">Sign up</a></p>
      </div>
      <p v-if="error" class="login-error">{{ error }}</p>
    </div>
  </div>
</template>
