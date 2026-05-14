import api from './index'

export const getUserPage = (params) => api.get('/system/user/page', { params })

export const getUserById = (id) => api.get(`/system/user/${id}`)

export const createUser = (data) => api.post('/system/user/create', data)

export const updateUser = (data) => api.put('/system/user/update', data)

export const deleteUser = (id) => api.delete(`/system/user/${id}`)

export const updateUserStatus = (data) => api.put('/system/user/status', data)

export const resetPassword = (data) => api.put('/system/user/reset-password', data)

export const getUserRoles = (userId) => api.get(`/system/user/role/${userId}`)

export const assignUserRoles = (data) => api.put('/system/user/role/assign', data)
