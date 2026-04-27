import api from './axios';

export const getPopularBooks = (limit = 10, year) =>
  api.get('/loans/statistics/popular-books', { params: { limit, year } });

export const getSummaryStatistics = () =>
  api.get('/loans/statistics/summary');