export const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', { hour12: false })
}

export const formatRate = (rate) => {
  if (rate === undefined || rate === null) return '-'
  return (rate * 100).toFixed(2) + '%'
}

export const parseJsonField = (jsonStr) => {
  try {
    return JSON.parse(jsonStr)
  } catch {
    return {}
  }
}

export const stringifyJson = (obj) => {
  return JSON.stringify(obj, null, 2)
}
