import api from './index'

export const calculateScore = (data) => api.post('/score/calculate', data)
export const submitScore = (id) => api.put(`/score/submit/${id}`)
export const reviewScore = (id, data) => api.put(`/score/review/${id}`, data)
export const getScoreDetail = (studentTopicId) => api.get(`/score/detail/${studentTopicId}`)
export const getBatchScores = (grade) => api.get('/score/batch/0', { params: { grade } })
