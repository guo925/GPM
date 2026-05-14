import api from './index'

export const getTopicPage = (params) => api.get('/topic/page', { params })
export const createTopic = (data) => api.post('/topic/create', data)
export const updateTopic = (id, data) => api.put(`/topic/update/${id}`, data)
export const deleteTopic = (id) => api.delete(`/topic/${id}`)
export const reviewTopic = (data) => api.put('/topic/review', data)
