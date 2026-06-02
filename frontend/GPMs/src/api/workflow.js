import api from './index'

export const getWorkflowItems = (params) => api.get('/workflow/items', { params })
export const saveWorkflowItem = (data) => api.post('/workflow/item', data)
export const reviewWorkflowItem = (data) => api.put('/workflow/review', data)
export const deleteWorkflowItem = (id) => api.delete(`/workflow/item/${id}`)

export const getFeatureItems = (basePath, params) => api.get(basePath, { params })
export const saveFeatureItem = (basePath, data) => api.post(basePath, data)
export const reviewFeatureItem = (basePath, data) => api.put(`${basePath}/review`, data)
export const deleteFeatureItem = (basePath, id) => api.delete(`${basePath}/${id}`)
