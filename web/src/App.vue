<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const isAuthed = computed(() => Boolean(authStore.token))

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <main class="app-shell" :class="{ 'is-login': !isAuthed }">
    <aside v-if="isAuthed" class="sidebar">
      <div class="brand">
        <span class="brand-mark">悦</span>
        <div>
          <strong>悦医健康</strong>
          <small>MedCare Online</small>
        </div>
      </div>

      <nav class="nav-list">
        <RouterLink
          v-for="menu in authStore.menus"
          :key="menu.path"
          :to="menu.path"
          class="nav-link"
        >
          <span>{{ menu.title }}</span>
        </RouterLink>
      </nav>

      <div class="account">
        <div>
          <strong>{{ authStore.user?.realName }}</strong>
          <small>{{ authStore.user?.roles.join(' / ') }}</small>
        </div>
        <button type="button" class="ghost-button" @click="logout">退出</button>
      </div>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </main>
</template>
