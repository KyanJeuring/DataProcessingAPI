import { createRouter, createWebHistory } from "vue-router";

import SignupView from "../views/SignupView.vue";
import LoginView from "../views/LoginView.vue";
import VerifyView from "../views/VerifyView.vue";
import DashboardView from "../views/DashboardView.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/signup", component: SignupView },
  { path: "/verify", component: VerifyView },
  { path: "/login", component: LoginView },
  { path: "/dashboard", component: DashboardView },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

