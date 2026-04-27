import api from './axios';

export const getBooks = (page = 0, size = 10) =>
  api.get('/books', { params: { page, size } });

export const getBook = (id) => api.get(`/books/${id}`);

export const createBook = (book) => api.post('/books', book);

export const updateBook = (id, book) => api.put(`/books/${id}`, book);

export const deleteBook = (id) => api.delete(`/books/${id}`);

export const searchBooks = (params) =>
  api.get('/books/search', { params });

export const getBookCopies = (bookId) =>
  api.get('/item-copies', { params: { libraryItemId: bookId } });

export const addCopy = (data) => api.post('/item-copies', data);

export const updateCopyStatus = (id, status) =>
  api.patch(`/item-copies/${id}/status`, { status });