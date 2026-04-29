<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiGet, apiPut } from '@/api/client'
import type { PageResult, UserSummary } from '@/types/api'

const keyword = ref('')
const roleCode = ref('')
const users = ref<UserSummary[]>([])
const total = ref(0)

async function loadUsers() {
  const page = await apiGet<PageResult<UserSummary>>('/api/admin/users', {
    current: 1,
    pageSize: 30,
    keyword: keyword.value,
    roleCode: roleCode.value,
  })
  users.value = page.records
  total.value = page.total
}

async function toggleStatus(user: UserSummary) {
  await apiPut<boolean>(`/api/admin/users/${user.id}/status`, {
    status: user.status === 1 ? 0 : 1,
  })
  await loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>用户管理</h1>
        <p>查看患者、医生、药剂师和管理员账号。</p>
      </div>
      <strong>{{ total }} 个账号</strong>
    </div>

    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索账号、姓名、手机号" @keyup.enter="loadUsers" />
      <select v-model="roleCode" @change="loadUsers">
        <option value="">全部角色</option>
        <option value="PATIENT">患者</option>
        <option value="DOCTOR">医生</option>
        <option value="PHARMACIST">药剂师</option>
        <option value="ADMIN">管理员</option>
      </select>
      <button class="primary-button" type="button" @click="loadUsers">查询</button>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>账号</th>
            <th>姓名</th>
            <th>电话</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.username }}</td>
            <td>{{ user.realName }}</td>
            <td>{{ user.phone }}</td>
            <td>
              <span v-for="role in user.roles" :key="role" class="tag">{{ role }}</span>
            </td>
            <td>
              <span :class="['status-pill', user.status === 1 ? 'success' : 'muted']">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="ghost-button" type="button" @click="toggleStatus(user)">
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

