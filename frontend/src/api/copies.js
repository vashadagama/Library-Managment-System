import api from './axios';

export const getCopies = (libraryItemId, status) =>
  api.get('/item-copies', {
    params: {
      ...(libraryItemId ? { libraryItemId } : {}),
      ...(status ? { status } : {}),
    },
  });

export const getCopy = (id) => api.get(`/item-copies/${id}`);

export const createCopy = (data) => api.post('/item-copies', data);

export const updateCopyStatus = (id, status) =>
  api.patch(`/item-copies/${id}/status`, { status });

export const deleteCopy = (id) => api.delete(`/item-copies/${id}`);
