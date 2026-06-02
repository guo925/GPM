import api from './index'

export const uploadFile = (file, bizType = 'common') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return api.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

export const getFileViewUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `/api/file/view?path=${encodeURIComponent(path)}`
}
