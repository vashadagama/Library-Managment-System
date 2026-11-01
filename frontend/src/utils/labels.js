const GENRE_LABELS = {
  FICTION: 'Художественная',
  CHILDREN: 'Детская',
  SCIENTIFIC: 'Научная',
  POPULAR_SCIENTIFIC: 'Научно-популярная',
};

const ITEM_STATUS_LABELS = {
  AVAILABLE: 'Доступно',
  BORROWED: 'Выдано',
  UNDER_REPAIR: 'На ремонте',
  LOST: 'Утеряно',
  WITHDRAWN: 'Списано',
};

const LOAN_STATUS_LABELS = {
  ACTIVE: 'Активна',
  RETURNED: 'Возвращена',
  RENEWED: 'Продлена',
  OVERDUE: 'Просрочена',
  LOST: 'Утеряна',
};

const USER_STATUS_LABELS = {
  ACTIVE: 'Активен',
  BLOCKED: 'Заблокирован',
};

const USER_ROLE_LABELS = {
  ADMIN: 'Администратор',
  LIBRARIAN: 'Библиотекарь',
  READER: 'Читатель',
};

const label = (dictionary, value) => dictionary[value] || value || '—';

export const genreLabel = (value) => label(GENRE_LABELS, value);
export const itemStatusLabel = (value) => label(ITEM_STATUS_LABELS, value);
export const loanStatusLabel = (value) => label(LOAN_STATUS_LABELS, value);
export const userStatusLabel = (value) => label(USER_STATUS_LABELS, value);
export const userRoleLabel = (value) => label(USER_ROLE_LABELS, value);

export const statusTone = (value) => {
  switch (String(value || '').toUpperCase()) {
    case 'ACTIVE':
    case 'AVAILABLE':
    case 'RETURNED':
      return 'success';
    case 'OVERDUE':
    case 'LOST':
    case 'BLOCKED':
      return 'danger';
    case 'RENEWED':
    case 'UNDER_REPAIR':
      return 'warning';
    case 'BORROWED':
    case 'WITHDRAWN':
      return 'info';
    default:
      return 'neutral';
  }
};
