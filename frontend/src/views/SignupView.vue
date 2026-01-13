<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const name = ref("");
const email = ref("");
const password = ref("");

const message = ref("");
const error = ref("");
const loading = ref(false);

const base = (import.meta.env.VITE_BACKEND_URL || "/api").replace(/\/$/, "");

async function signup() {
  message.value = "";
  error.value = "";
  loading.value = true;

  try {
    const res = await fetch(`${base}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: name.value,
        email: email.value,
        password: password.value,
      }),
    });

    const contentType = res.headers.get("content-type") || "";
    const body = contentType.includes("application/json")
      ? await res.json().catch(() => ({}))
      : await res.text().catch(() => "");

    if (!res.ok) {
      const backendMsg =
        (body && typeof body === "object" && (body.message || body.error)) ||
        (typeof body === "string" && body) ||
        "";
      throw new Error(`HTTP ${res.status} ${res.statusText}${backendMsg ? ` — ${backendMsg}` : ""}`);
    }

    message.value =
      (typeof body === "string" && body) ||
      (body && typeof body === "object" && (body.message || JSON.stringify(body))) ||
      "Registered. Verification code sent.";

    sessionStorage.setItem("pendingEmail", email.value);
    router.push("/verify");
  } catch (e) {
    error.value = e?.message || "Signup failed";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <h2>Signup</h2>

  <form @submit.prevent="signup">
    <input v-model="name" placeholder="Name" required />
    <input v-model="email" type="email" placeholder="Email" required />
    <input v-model="password" type="password" placeholder="Password" required />
    <button type="submit" :disabled="loading">{{ loading ? "..." : "Signup" }}</button>
  </form>

  <p v-if="message">{{ message }}</p>
  <p v-if="error">{{ error }}</p>
</template>
