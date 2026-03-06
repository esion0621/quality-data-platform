import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

// 规则管理
export const getRules = () => api.get('/rules')
export const getRule = (id) => api.get(`/rules/${id}`)
export const createRule = (data) => api.post('/rules', data)
export const updateRule = (id, data) => api.put(`/rules/${id}`, data)
export const deleteRule = (id) => api.delete(`/rules/${id}`)
export const triggerRule = (id) => api.post(`/rules/trigger/${id}`)
export const getRealtimeRules = () => api.get('/rules/realtime')

// 任务实例
export const getTasks = () => api.get('/tasks')
export const getTasksByRule = (ruleId) => api.get(`/tasks/rule/${ruleId}`)
export const getTask = (id) => api.get(`/tasks/${id}`)

// 离线告警
export const getAlerts = () => api.get('/alerts')
export const getAlertsByRule = (ruleId) => api.get(`/alerts/rule/${ruleId}`)
export const confirmAlert = (id, confirmBy) => api.put(`/alerts/${id}/confirm`, null, { params: { confirmBy } })

// 实时告警
export const getRealtimeAlerts = (params) => api.get('/realtime-alerts', { params })

// 统计
export const getTrend = (start, end) => api.get('/stats/trend', { params: { start, end } })
export const getPassRates = () => api.get('/stats/pass-rates')
export const getRealtimeLatest = () => api.get('/stats/realtime/latest')
