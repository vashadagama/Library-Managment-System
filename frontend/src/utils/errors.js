export const getErrorMessage = (error, fallback = 'Не удалось выполнить действие') => {
  const data = error?.response?.data;

  if (data?.errors && typeof data.errors === 'object') {
    const details = Object.values(data.errors).filter(Boolean);
    if (details.length > 0) return details.join('; ');
  }

  if (data?.message) return data.message;
  if (error?.message) return error.message;

  return fallback;
};
