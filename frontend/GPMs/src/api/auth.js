import api from './index'

export const login = (data) => api.post('/auth/login', data)

export const me = () => api.get('/auth/me')

export const logout = () => api.post('/auth/logout')
