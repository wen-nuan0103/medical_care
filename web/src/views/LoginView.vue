<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('admin')
const password = ref('123456')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await authStore.login(username.value, password.value)
    await router.push(authStore.homePath)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}

function fillDemo(name: string) {
  username.value = name
  password.value = '123456'
}
</script>

<template>
  <section class="login-page">
    <div class="login-panel">
      <div class="login-heading">
        <span class="brand-mark">悦</span>
        <div>
          <h1>悦医健康</h1>
          <p>在线医疗健康平台</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label>
          账号
          <input v-model="username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="password" type="password" autocomplete="current-password" />
        </label>
        <p v-if="error" class="error-text">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="demo-users">
        <button type="button" @click="fillDemo('admin')">管理员</button>
        <button type="button" @click="fillDemo('patient_li')">患者</button>
        <button type="button" @click="fillDemo('doctor_zhang')">医生</button>
        <button type="button" @click="fillDemo('pharmacist_chen')">药剂师</button>
      </div>
    </div>
  </section>
</template>

