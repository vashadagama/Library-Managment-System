import api from './axios';

export const getReaders = () => api.get('/users');

export const createReader = (data) => api.post('/users', data);

export const changeUserStatus = (id, status) =>
  api.patch(`/users/${id}/status`, { status });

export const updateMaxLoans = (id, maxActiveLoans) =>
  api.patch(`/users/${id}/max-loans`, { maxActiveLoans });