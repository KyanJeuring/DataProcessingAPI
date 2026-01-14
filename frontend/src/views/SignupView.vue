<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import "../style/signupview.css";
const router = useRouter();

const username = ref("");
const firstName = ref("");
const lastName = ref("");
const email = ref("");
const password = ref("");
const role = ref("");
const theme = ref("");                 
const notifications = ref(null);       
const language = "en";                
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
        username: username.value,
        firstName: firstName.value,
        lastName: lastName.value,
        email: email.value,
        password: password.value,
        roles: [role.value],

        preferences: {
          theme: theme.value,
          language,
          notifications: notifications.value,
        },
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
      throw new Error(
        `HTTP ${res.status} ${res.statusText}${backendMsg ? ` — ${backendMsg}` : ""}`
      );
    }

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
  <div class="signup-page">
    <div class="signup-card">
      <h2>FleetMaster Signup</h2>
      <p class="subtitle">Create your company account</p>

      <form @submit.prevent="signup">
        <div class="row">
          <input v-model="username" placeholder="Username" required />
        </div>

        <div class="row two">
          <input v-model="firstName" placeholder="First name" required />
          <input v-model="lastName" placeholder="Last name" required />
        </div>

        <div class="row">
          <input v-model="email" type="email" placeholder="Email" required />
        </div>

        <div class="row">
          <input v-model="password" type="password" placeholder="Password" required />
        </div>

        <div class="row">
          <select v-model="role" required>
            <option disabled value="">Select role</option>
            <option value="ADMIN">Admin</option>
            <option value="MANAGER">Manager</option>
            <option value="VIEWER">Viewer</option>
            <option value="DRIVER">Driver</option>
          </select>
        </div>

        <div class="preferences">
          <div class="pref-group">
            <span>Theme</span>
            <label><input type="radio" value="light" v-model="theme" required /> Light</label>
            <label><input type="radio" value="dark" v-model="theme" required /> Dark</label>
          </div>

          <div class="pref-group">
            <span>Notifications</span>
            <label><input type="radio" :value="true" v-model="notifications" required /> Yes</label>
            <label><input type="radio" :value="false" v-model="notifications" required /> No</label>
          </div>

          <div class="pref-group disabled">
            <span>Language</span>
            <span class="fixed">English (EN)</span>
          </div>
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? "Creating account..." : "Create Account" }}
        </button>

        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </div>
</template>
