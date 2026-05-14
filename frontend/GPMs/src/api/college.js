import api from './index'

export const getCollegeList = () => api.get('/college/list')
export const createCollege = (data) => api.post('/college/create', data)
export const updateCollege = (id, data) => api.put(`/college/update/${id}`, data)
export const deleteCollege = (id) => api.delete(`/college/${id}`)
