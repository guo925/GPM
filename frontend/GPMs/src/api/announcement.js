import api from './index'

export const getAnnouncementPage = (params) => api.get('/announcement/page', { params })
export const createAnnouncement = (data) => api.post('/announcement', data)
export const updateAnnouncement = (id, data) => api.put(`/announcement/${id}`, data)
export const deleteAnnouncement = (id) => api.delete(`/announcement/${id}`)
