import api from './axios';

export const issueByLibraryItem = (userId, libraryItemId) =>
  api.post('/loans/issue/by-item', null, { params: { userId, libraryItemId } });

export const issueByCopy = (userId, copyId) =>
  api.post('/loans/issue/by-copy', null, { params: { userId, copyId } });

export const returnBook = (loanId) =>
  api.post('/loans/return', null, { params: { loanId } });

export const renewLoan = (loanId, extraDays = 14) =>
  api.post(`/loans/${loanId}/renew`, null, { params: { extraDays } });

export const getOverdueLoans = () => api.get('/loans/overdue');

export const getLoanHistoryByUser = (userId) =>
  api.get(`/loans/history/user/${userId}`);

export const getLoanHistoryByCopy = (copyId) =>
  api.get(`/loans/history/copy/${copyId}`);

export const getActiveLoansByUser = (userId) =>
  api.get(`/loans/active/user/${userId}`);

export const getPopularBooks = (limit = 10, year) =>
  api.get('/loans/statistics/popular-books', { params: { limit, year } });

export const getSummaryStatistics = () =>
  api.get('/loans/statistics/summary');