import api from './index'

export const exportScores = (batchId) => api.get(`/export/scores/${batchId}`)
