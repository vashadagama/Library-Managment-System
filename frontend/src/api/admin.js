import api from './axios';

export const getSystemParameters = () => api.get('/admin/system-parameters');

export const updateSystemParameter = (key, value) =>
  api.put(`/admin/system-parameters/${encodeURIComponent(key)}`, { value });

export const getDictionaryEntries = (type) => api.get(`/admin/dictionaries/${type}`);

export const createDictionaryEntry = (type, data) => api.post(`/admin/dictionaries/${type}`, data);

export const updateDictionaryEntry = (type, id, data) =>
  api.put(`/admin/dictionaries/${type}/${id}`, data);

export const deleteDictionaryEntry = (type, id) => api.delete(`/admin/dictionaries/${type}/${id}`);

export const getAuditLog = (page = 0, size = 20) => api.get('/audit', { params: { page, size } });
