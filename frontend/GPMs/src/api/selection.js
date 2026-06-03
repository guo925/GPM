import api from './index'

export const submitPreferences = (data) => api.post('/selection/submit', data)
export const getMySelections = (grade) => api.get('/selection/my', { params: { grade } })
export const getReviewList = (grade) => api.get('/selection/review-list', { params: { grade } })
export const teacherReview = (data) => api.put('/selection/review', data)
export const autoAllocate = (batchId, grade) => api.post(`/selection/auto-allocate/${batchId || 0}`, null, { params: { grade } })
