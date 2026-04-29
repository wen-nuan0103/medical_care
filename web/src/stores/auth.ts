import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { apiGet, apiPost } from '@/api/client'

export interface CurrentUser {
  id: number
  username: string
  realName: string
  roles: string[]
}

export interface MenuItem {
  title: string
  path: string
  icon: string
  children: MenuItem[]
}

interface LoginResponse {
  token: string
  user: CurrentUser
  menus: MenuItem[]
}

const TOKEN_KEY = 'medical_care_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<CurrentUser | null>(null)
  const menus = ref<MenuItem[]>([])

  const homePath = computed(() => {
    const roles = user.value?.roles || []
    if (roles.includes('ADMIN')) return '/admin/dashboard'
    if (roles.includes('PHARMACIST')) return '/pharmacist/workbench'
    if (roles.includes('DOCTOR')) return '/doctor/workbench'
    return '/patient/home'
  })

  async function login(username: string, password: string) {
    const data = await apiPost<LoginResponse>('/api/auth/login', { username, password })
    token.value = data.token
    user.value = data.user
    menus.value = data.menus
    localStorage.setItem(TOKEN_KEY, data.token)
  }

  async function hydrate() {
    if (!token.value || user.value) return
    try {
      user.value = await apiGet<CurrentUser>('/api/auth/me')
      menus.value = await apiGet<MenuItem[]>('/api/auth/menus')
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    menus.value = []
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, user, menus, homePath, login, hydrate, logout }
})

