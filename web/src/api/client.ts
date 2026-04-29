interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

function buildQuery(query?: Record<string, unknown>) {
  if (!query) return ''
  const params = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value))
    }
  })
  const queryString = params.toString()
  return queryString ? `?${queryString}` : ''
}

async function request<T>(path: string, options: RequestInit = {}, query?: Record<string, unknown>): Promise<T> {
  const token = localStorage.getItem('medical_care_token')
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${path}${buildQuery(query)}`, {
    ...options,
    headers,
  })
  const payload = (await response.json()) as ApiResponse<T>
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

export function apiGet<T>(path: string, query?: Record<string, unknown>) {
  return request<T>(path, { method: 'GET' }, query)
}

export function apiPost<T>(path: string, body?: unknown) {
  return request<T>(path, {
    method: 'POST',
    body: JSON.stringify(body ?? {}),
  })
}

export function apiPut<T>(path: string, body?: unknown) {
  return request<T>(path, {
    method: 'PUT',
    body: JSON.stringify(body ?? {}),
  })
}

