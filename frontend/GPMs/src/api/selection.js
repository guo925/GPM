import api from './index'

export const submitPreferences = (data) => api.post('/selection/submit', data)
export const getMySelections = (batchId) => api.get('/selection/my', { params: { batchId } })
export const getReviewList = (batchId) => api.get('/selection/review-list', { params: { batchId } })
export const teacherReview = (data) => api.put('/selection/review', data)
export const autoAllocate = (batchId) => api.post(`/selection/auto-allocate/${batchId}`)
