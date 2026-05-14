import api from './index'

export const getMajorList = (collegeId) => api.get('/major/list', { params: { collegeId } })
export const createMajor = (data) => api.post('/major/create', data)
export const updateMajor = (id, data) => api.put(`/major/update/${id}`, data)
export const deleteMajor = (id) => api.delete(`/major/${id}`)
