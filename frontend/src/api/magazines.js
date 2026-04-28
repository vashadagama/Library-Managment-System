import api from './axios';

export const getMagazines = (page = 0, size = 10, filters = {}) => {
  const params = { page, size, ...filters };
  return api.get('/magazines', { params });
};

export const getMagazine = (id) => api.get(`/magazines/${id}`);

export const createMagazine = (data) => api.post('/magazines', data);

export const updateMagazine = (id, data) => api.put(`/magazines/${id}`, data);

export const deleteMagazine = (id) => api.delete(`/magazines/${id}`);

export const getMagazineCopies = (magId) =>
  api.get('/item-copies', { params: { libraryItemId: magId } });

export const addCopy = (data) => api.post('/item-copies', data);

export const updateCopyStatus = (id, status) =>
  api.patch(`/item-copies/${id}/status`, { status });