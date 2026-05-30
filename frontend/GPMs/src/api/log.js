import api from './index'

export const getLogPage = (params) => api.get('/log/operation/page', { params })
export const getLogStatistics = () => api.get('/log/operation/statistics')
export const getAuditLogPage = (params) => api.get('/log/audit/page', { params })
