import api from './index'

export const getNotificationPage = (params) => api.get('/notification/page', { params })
export const getUnreadCount = () => api.get('/notification/unread-count')
export const markNotificationRead = (id) => api.put(`/notification/read/${id}`)
