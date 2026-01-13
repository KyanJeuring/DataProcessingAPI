<script setup>
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

    if (!res.ok) throw new Error("Login failed");

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
  <h2>Login</h2>

  <form @submit.prevent="login">
    <input v-model="email" type="email" placeholder="Email" />
    <input v-model="password" type="password" placeholder="Password" />
    <button type="submit">Login</button>
  </form>

  <p>
    Don't have an account?
    <a href="/signup">Sign up</a>
  </p>

  <p>{{ error }}</p>
</template>
