export const formatDate = (value) => {
  if (!value) return '—';

  const text = String(value);
  const match = text.match(/^(\d{4})-(\d{2})-(\d{2})/);

  if (match) {
    return `${match[3]}.${match[2]}.${match[1]}`;
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return text;

  return date.toLocaleDateString('ru-RU');
};

export const formatDateTime = (value) => {
  if (!value) return '—';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);

  return date.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatMoney = (value) => {
  if (value == null || value === '') return '—';

  const number = Number(value);
  if (Number.isNaN(number)) return String(value);

  return `${number.toLocaleString('ru-RU', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ₽`;
};

export const plural = (count, one, few, many) => {
  const value = Math.abs(Number(count)) % 100;
  const last = value % 10;

  if (value > 10 && value < 20) return many;
  if (last === 1) return one;
  if (last >= 2 && last <= 4) return few;
  return many;
};

export const formatCount = (count, one, few, many) =>
  `${count} ${plural(count, one, few, many)}`;
