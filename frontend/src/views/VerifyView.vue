<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const email = ref(sessionStorage.getItem("pendingEmail") || "");
const code = ref("");

const loading = ref(false);
const error = ref("");
const message = ref("");

const base = (import.meta.env.VITE_BACKEND_URL || "/api").replace(/\/$/, "");

async function verify() {
  loading.value = true;
  error.value = "";
  message.value = "";

  try {
    if (!email.value) {
      throw new Error("Missing email. Go back to Signup and try again.");
    }

    const res = await fetch(`${base}/auth/verify/code/check`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email.value,
        code: code.value,
      }),
    });

    const text = await res.text().catch(() => "");
    if (!res.ok) throw new Error(`HTTP ${res.status} — ${text || "Verification failed"}`);

    message.value = text || "Verified!";
    router.push("/login");
  } catch (e) {
    error.value = e?.message || "Verification failed";
  } finally {
    loading.value = false;
  }
}

async function resend() {
  loading.value = true;
  error.value = "";
  message.value = "";

  try {
    if (!email.value) throw new Error("Missing email. Go back to Signup.");

    const res = await fetch(`${base}/auth/verify/code/send`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email.value }),
    });

    const text = await res.text().catch(() => "");
    if (!res.ok) throw new Error(`HTTP ${res.status} — ${text || "Resend failed"}`);

    message.value = text || "Code sent.";
  } catch (e) {
    error.value = e?.message || "Resend failed";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <h2>Verify</h2>

  <p>Email: {{ email }}</p>

  <form @submit.prevent="verify">
    <input v-model="code" placeholder="Verification code" required />
    <button type="submit" :disabled="loading">{{ loading ? "..." : "Verify" }}</button>
  </form>

  <button @click="resend" :disabled="loading">Resend code</button>

  <p v-if="message">{{ message }}</p>
  <p v-if="error">{{ error }}</p>
</template>
