import api from './axios';

const download = async (url, fileName) => {
  const response = await api.get(url, { responseType: 'blob' });
  const blobUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = blobUrl;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
};

export const downloadPopularBooksReport = (year) =>
  download(`/reports/popular-books${year ? `?year=${year}` : ''}`, 'otchet-populyarnye-izdaniya.pdf');

export const downloadActiveReadersReport = () =>
  download('/reports/active-readers', 'otchet-aktivnye-chitateli.pdf');

export const downloadFineDebtsReport = () =>
  download('/reports/fine-debts', 'otchet-zadolzhennosti-po-shtrafam.pdf');

export const downloadOverdueLoansReport = () =>
  download('/reports/overdue-loans', 'otchet-prosrochennye-vydachi.pdf');
