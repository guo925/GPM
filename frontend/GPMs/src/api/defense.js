import api from './index'

export const getDefenseBatches = (grade) => api.get('/defense/batches', { params: { grade } })
export const createDefenseBatch = (data) => api.post('/defense/batch/create', data)
export const deleteDefenseBatch = (id) => api.delete(`/defense/batch/${id}`)
export const getDefenseGroups = (defenseBatchId) => api.get('/defense/groups', { params: { defenseBatchId } })
export const createDefenseGroup = (data) => api.post('/defense/group/create', data)
export const deleteDefenseGroup = (id) => api.delete(`/defense/group/${id}`)
export const getDefenseArrangements = (groupId) => api.get('/defense/arrangements', { params: { groupId } })
export const addDefenseArrangement = (data) => api.post('/defense/arrange', data)
export const deleteDefenseArrangement = (id) => api.delete(`/defense/arrange/${id}`)
export const saveDefenseResult = (data) => api.post('/defense/result', data)
export const getDefenseResult = (arrangementId) => api.get(`/defense/result/${arrangementId}`)
