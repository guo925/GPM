import api from './index'

export const getBatchPage = (params) => api.get('/batch/page', { params })
export const getCurrentBatch = () => api.get('/batch/current')
export const getBatchDetail = (id) => api.get(`/batch/${id}`)
export const createBatch = (data) => api.post('/batch/create', data)
export const updateBatch = (data) => api.put('/batch/update', data)
export const deleteBatch = (id) => api.delete(`/batch/${id}`)
export const advanceStage = (id, stage) => api.put(`/batch/${id}/stage`, null, { params: { stage } })
export const getDistinctGrades = () => api.get('/batch/grades')
export const getBatchesByGrade = (grade) => api.get('/batch/by-grade', { params: { grade } })
