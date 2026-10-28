import api from './axios';

export const getReaders = (page = 0, size = 10) =>
  api.get('/users', { params: { page, size } });

export const createReader = (data) => api.post('/users', data);

export const changeUserStatus = (id, status) =>
  api.patch(`/users/${id}/status`, { status });

export const updateMaxLoans = (id, maxActiveLoans) =>
  api.patch(`/users/${id}/max-loans`, { maxActiveLoans });

export const searchReaders = (query) => api.get('/users/search', { params: { q: query } });