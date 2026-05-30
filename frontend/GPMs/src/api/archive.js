import api from './index'

export const archiveBatch = (batchId) => api.post(`/archive/batch/${batchId}`)
export const archiveHistoryBatches = () => api.post('/archive/history-batches')
export const getArchiveLogs = (limit = 20) => api.get('/archive/logs', { params: { limit } })
