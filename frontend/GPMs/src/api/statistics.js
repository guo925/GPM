import api from './index'

export const getOverview = () => api.get('/statistics/overview')
