import api from './index'

export const getRolePage = (params) => api.get('/system/role/page', { params })

export const getRoleList = () => api.get('/system/role/list')

export const getRoleById = (id) => api.get(`/system/role/${id}`)

export const createRole = (data) => api.post('/system/role/create', data)

export const updateRole = (data) => api.put('/system/role/update', data)

export const deleteRole = (id) => api.delete(`/system/role/${id}`)

export const getPermissionList = () => api.get('/system/permission/list')
