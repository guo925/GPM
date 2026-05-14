import api from './index'

export const getStudentTopicPage = (params) => api.get('/student-topic/page', { params })
export const getMyTopic = () => api.get('/student-topic/my')
