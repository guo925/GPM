import api from './index'

export const submitProcess = (data) => api.post('/process/submit', data)
export const reviewProcess = (data) => api.put('/process/review', data)
export const getProcessList = (studentTopicId) => api.get(`/process/list/${studentTopicId}`)
export const getProcessStage = (studentTopicId, stage) => api.get('/process/stage', { params: { studentTopicId, stage } })
