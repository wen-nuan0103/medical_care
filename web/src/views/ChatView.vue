<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getConsultationDetail, getHistoryMessages, endConsultation, giftTime } from '@/api/consultation'
import type { ConsultationSession, ChatMessage } from '@/types/api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const sessionId = Number(route.params.sessionId)
const session = ref<ConsultationSession | null>(null)
const messages = ref<ChatMessage[]>([])
const newMsg = ref('')
const loading = ref(true)
const sending = ref(false)
const chatBox = ref<HTMLElement | null>(null)

// WebSocket
let ws: WebSocket | null = null
// 跟踪乐观消息的临时 ID，收到服务端回显时替换
const pendingTempIds = new Set<number>()

const isDoctor = computed(() => auth.user?.roles.includes('DOCTOR'))
const peerId = computed(() => {
  if (!session.value || !auth.user) return null
  return isDoctor.value ? session.value.patientId : session.value.doctorId
})
const isEnded = computed(() => session.value?.status === 'ENDED' || session.value?.status === 'CANCELED')

onMounted(async () => {
  try {
    session.value = await getConsultationDetail(sessionId)
    messages.value = await getHistoryMessages(sessionId)
    await nextTick()
    scrollToBottom()
    connectWs()
  } catch { /* empty */ } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  ws?.close()
})

function connectWs() {
  const token = localStorage.getItem('medical_care_token')
  if (!token) return
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  ws = new WebSocket(`${protocol}://${location.host}/ws?token=${token}`)
  ws.onmessage = (evt) => {
    try {
      const resp = JSON.parse(evt.data)
      if (resp.code === 0 && resp.data) {
        const msg = resp.data as ChatMessage
        if (msg.sessionId === sessionId) {
          // 如果是自己发送的消息回显，替换掉乐观消息
          if (msg.senderId === auth.user?.id && pendingTempIds.size > 0) {
            // 找到第一个临时消息并替换
            const tempId = pendingTempIds.values().next().value
            if (tempId !== undefined) {
              const idx = messages.value.findIndex(m => m.id === tempId)
              if (idx >= 0) {
                messages.value[idx] = msg
                pendingTempIds.delete(tempId)
                return
              }
            }
          }
          // 否则是对方发来的新消息，直接追加
          if (!messages.value.find(m => m.id === msg.id)) {
            messages.value.push(msg)
            nextTick(scrollToBottom)
          }
        }
      } else if (resp.code !== 0) {
        alert(resp.message || '发送失败')
        // 刷新状态和消息列表，清除无效的乐观消息
        pendingTempIds.clear()
        getConsultationDetail(sessionId).then(res => {
          session.value = res
        }).catch(() => {})
        getHistoryMessages(sessionId).then(res => {
          messages.value = res
        }).catch(() => {})
      }
    } catch { /* ignore */ }
  }
  ws.onerror = () => { /* reconnect later */ }
}

function scrollToBottom() {
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

async function send() {
  if (!newMsg.value.trim() || sending.value || isEnded.value) return
  sending.value = true
  const payload = {
    sessionId,
    receiverId: peerId.value,
    messageType: 'TEXT',
    content: newMsg.value.trim(),
  }
  const tempId = Date.now()
  try {
    ws?.send(JSON.stringify(payload))
    // 乐观插入，标记临时 ID
    pendingTempIds.add(tempId)
    messages.value.push({
      id: tempId,
      sessionId,
      senderId: auth.user!.id,
      senderName: auth.user!.realName,
      senderAvatar: null,
      receiverId: peerId.value!,
      messageType: 'TEXT',
      content: payload.content,
      attachmentId: null,
      attachmentUrl: null,
      readStatus: 0,
      sendTime: new Date().toISOString(),
    })
    newMsg.value = ''
    await nextTick()
    scrollToBottom()
  } finally {
    sending.value = false
  }
}

async function handleEnd() {
  if (!confirm('确定要结束本次问诊吗？')) return
  await endConsultation(sessionId)
  session.value = await getConsultationDetail(sessionId)
}

// 赠送时长
const showGift = ref(false)
const giftMin = ref(10)
const giftReason = ref('')
async function handleGift() {
  if (giftMin.value < 1) return
  await giftTime(sessionId, giftMin.value, giftReason.value || '补充沟通时间')
  session.value = await getConsultationDetail(sessionId)
  showGift.value = false
}

function statusLabel(s: string) {
  const m: Record<string, string> = { WAITING: '等待接诊', IN_PROGRESS: '问诊中', ENDED: '已结束', CANCELED: '已取消' }
  return m[s] ?? s
}
function statusClass(s: string) {
  if (s === 'IN_PROGRESS') return 'success'
  if (s === 'WAITING') return 'warning'
  return 'muted'
}
function formatTime(s: string | null) {
  if (!s) return ''
  return s.replace('T', ' ').slice(11, 16)
}
</script>

<template>
  <section class="chat-page">
    <div v-if="loading" class="empty-text">加载中...</div>
    <template v-else-if="session">
      <!-- 顶栏 -->
      <div class="chat-topbar">
        <button class="ghost-button" type="button" @click="router.back()">← 返回</button>
        <div class="topbar-info">
          <strong>{{ isDoctor ? session.patientName : session.doctorName }}</strong>
          <span :class="['status-pill', statusClass(session.status)]">{{ statusLabel(session.status) }}</span>
        </div>
        <div class="topbar-time">
          <span>{{ session.usedMinutes }}/{{ session.allowedMinutes }} 分钟</span>
        </div>
        <div class="topbar-actions">
          <button v-if="isDoctor && !isEnded" class="ghost-button" type="button" @click="showGift = true">赠送时长</button>
          <button v-if="!isEnded" class="ghost-button danger-text" type="button" @click="handleEnd">结束问诊</button>
        </div>
      </div>

      <!-- 消息区 -->
      <div ref="chatBox" class="chat-body">
        <div v-for="msg in messages" :key="msg.id"
          :class="['msg-row', msg.senderId === auth.user?.id ? 'msg-self' : 'msg-other']"
        >
          <div class="msg-avatar">{{ (msg.senderName || '?').slice(-1) }}</div>
          <div class="msg-bubble">
            <small class="msg-name">{{ msg.senderName }} · {{ formatTime(msg.sendTime) }}</small>
            <p>{{ msg.content }}</p>
          </div>
        </div>
        <div v-if="messages.length === 0" class="empty-text" style="text-align:center;padding:40px">暂无消息，发送第一条消息开始问诊吧</div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-bar">
        <input
          v-model="newMsg"
          :disabled="isEnded"
          :placeholder="isEnded ? '问诊已结束' : '输入消息...'"
          @keyup.enter="send"
        />
        <button class="primary-button" type="button" :disabled="isEnded || !newMsg.trim()" @click="send">发送</button>
      </div>

      <!-- 赠送时长对话框 -->
      <div v-if="showGift" class="modal-overlay" @click.self="showGift = false">
        <div class="modal-panel">
          <h3>赠送聊天时长</h3>
          <label>
            赠送分钟数
            <input v-model.number="giftMin" type="number" min="1" />
          </label>
          <label>
            赠送原因
            <input v-model="giftReason" placeholder="如：病情复杂，补充沟通" />
          </label>
          <div class="modal-footer">
            <button class="ghost-button" type="button" @click="showGift = false">取消</button>
            <button class="primary-button" type="button" @click="handleGift">确认赠送</button>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px);
  max-height: 100%;
}
.chat-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--line);
  flex-wrap: wrap;
}
.topbar-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.topbar-info strong {
  font-size: 16px;
}
.topbar-time {
  color: var(--muted);
  font-size: 13px;
}
.topbar-actions {
  display: flex;
  gap: 8px;
}
.danger-text {
  color: var(--danger) !important;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.msg-row {
  display: flex;
  gap: 10px;
  max-width: 75%;
}
.msg-self {
  margin-left: auto;
  flex-direction: row-reverse;
}
.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-weight: 700;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  font-size: 14px;
}
.msg-self .msg-avatar {
  background: var(--accent);
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--panel);
  border: 1px solid var(--line);
  min-width: 60px;
}
.msg-self .msg-bubble {
  background: #e7f5ee;
  border-color: #c6e9d6;
}
.msg-name {
  display: block;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 4px;
}
.msg-bubble p {
  margin: 0;
  line-height: 1.5;
  word-break: break-word;
}
.chat-input-bar {
  display: flex;
  gap: 10px;
  padding: 14px 0;
  border-top: 1px solid var(--line);
}
.chat-input-bar input {
  flex: 1;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: grid;
  place-items: center;
  z-index: 100;
}
.modal-panel {
  width: min(420px, 90vw);
  padding: 28px;
  border-radius: 12px;
  background: var(--panel);
  display: grid;
  gap: 16px;
}
.modal-panel h3 {
  margin: 0;
  font-size: 18px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
