import api from './axios';

export const getGenres = () => api.get('/dictionaries/genres');
export const getLanguages = () => api.get('/dictionaries/languages');
export const getPublishers = () => api.get('/dictionaries/publishers');
export const getLocations = () => api.get('/dictionaries/locations');
export const getRoles = () => api.get('/dictionaries/roles');
export const getUserStatuses = () => api.get('/dictionaries/user-statuses');
export const getItemStatuses = () => api.get('/dictionaries/item-statuses');
export const getLoanStatuses = () => api.get('/dictionaries/loan-statuses');
export const getNotificationTypes = () => api.get('/dictionaries/notification-types');
export const getAuditTypes = () => api.get('/dictionaries/audit-types');
