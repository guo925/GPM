import api from './index'

export const createGuidance = (data) => api.post('/guidance/create', data)
export const reviewGuidance = (id, comment) => api.put(`/guidance/review/${id}`, null, { params: { comment } })
export const getGuidanceList = (studentTopicId) => api.get(`/guidance/list/${studentTopicId}`)
