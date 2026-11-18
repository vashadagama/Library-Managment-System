import api from './axios';

export const getFines = (paid) =>
  api.get('/fines', { params: paid == null ? {} : { paid } });

export const payFine = (id) => api.patch(`/fines/${id}/pay`);
