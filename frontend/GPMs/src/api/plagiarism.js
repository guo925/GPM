import api from './index'

export const checkPlagiarism = (processInstanceId) => api.post('/plagiarism/check', { processInstanceId })
