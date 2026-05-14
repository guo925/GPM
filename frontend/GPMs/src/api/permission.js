import api from './index'

export const getPermissionList = () => api.get('/system/permission/list')
export const getPermissionTree = () => api.get('/system/permission/tree')
export const createPermission = (data) => api.post('/system/permission/create', data)
export const updatePermission = (data) => api.put('/system/permission/update', data)
export const deletePermission = (id) => api.delete(`/system/permission/${id}`)
