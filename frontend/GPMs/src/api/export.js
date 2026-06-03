import api from './index'

export const exportScores = (grade) => api.get('/export/scores', { params: { grade } })
