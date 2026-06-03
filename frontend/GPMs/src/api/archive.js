import api from './index'

export const archiveBatch = (grade) => api.post('/archive/batch/0', { params: { grade } })
export const archiveHistoryBatches = () => api.post('/archive/history-batches')
export const getArchiveLogs = (limit = 20) => api.get('/archive/logs', { params: { limit } })
