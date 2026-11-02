import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  changeUserStatus,
  createReader,
  getReaders,
  searchReaders,
  updateMaxLoans,
} from '../api/readers';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';
import { userRoleLabel, userStatusLabel, statusTone } from '../utils/labels';
import useDebouncedValue from '../hooks/useDebouncedValue';
import Dialog from '../components/Dialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';
import Pagination from '../components/Pagination';

const emptyReader = {
  firstName: '',
  lastName: '',
  middleName: '',
  email: '',
};

const ReadersScreen = () => {
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ROLE_ADMIN';

  const [readers, setReaders] = useState([]);
  const [search, setSearch] = useState(() => searchParams.get('search') || '');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [busyId, setBusyId] = useState(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState(emptyReader);
  const [createError, setCreateError] = useState('');
  const [creating, setCreating] = useState(false);
  const [limitTarget, setLimitTarget] = useState(null);
  const [limitValue, setLimitValue] = useState('');
  const [limitError, setLimitError] = useState('');
  const [savingLimit, setSavingLimit] = useState(false);
  const debouncedSearch = useDebouncedValue(search, 350);

  const loadReaders = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      if (debouncedSearch.trim()) {
        const response = await searchReaders(debouncedSearch.trim());
        const list = Array.isArray(response.data) ? response.data : [];
        setReaders(list);
        setTotalPages(list.length > 0 ? 1 : 0);
        setTotalElements(list.length);
      } else {
        const response = await getReaders(page, 10);
        setReaders(response.data?.content || []);
        setTotalPages(response.data?.totalPages || 0);
        setTotalElements(response.data?.totalElements || 0);
      }
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить читателей'));
      setReaders([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch]);

  useEffect(() => {
    loadReaders();
  }, [loadReaders, reloadKey]);

  const reload = () => setReloadKey((key) => key + 1);

  const handleStatus = async (reader) => {
    const nextStatus = reader.statusName === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    setBusyId(reader.id);

    try {
      await changeUserStatus(reader.id, nextStatus);
      toast.success(nextStatus === 'BLOCKED' ? 'Читатель заблокирован' : 'Читатель активирован');
      reload();
    } catch (statusError) {
      toast.error(getErrorMessage(statusError, 'Не удалось изменить статус'));
    } finally {
      setBusyId(null);
    }
  };

  const openCreate = () => {
    setCreateForm(emptyReader);
    setCreateError('');
    setCreateOpen(true);
  };

  const closeCreate = () => {
    if (creating) return;
    setCreateOpen(false);
    setCreateError('');
  };

  const saveReader = async () => {
    if (!createForm.firstName.trim() || !createForm.lastName.trim() || !createForm.email.trim()) {
      setCreateError('Заполните имя, фамилию и email');
      return;
    }

    setCreating(true);
    setCreateError('');

    try {
      await createReader({
        firstName: createForm.firstName.trim(),
        lastName: createForm.lastName.trim(),
        middleName: createForm.middleName.trim() || null,
        email: createForm.email.trim(),
        role: 'READER',
      });
      toast.success('Читатель зарегистрирован');
      setCreateOpen(false);
      setCreateForm(emptyReader);
      setPage(0);
      reload();
    } catch (createReaderError) {
      setCreateError(getErrorMessage(createReaderError, 'Не удалось создать читателя'));
    } finally {
      setCreating(false);
    }
  };

  const openLimit = (reader) => {
    setLimitTarget(reader);
    setLimitValue(String(reader.maxActiveLoans ?? 5));
    setLimitError('');
  };

  const saveLimit = async () => {
    if (!limitTarget) return;

    const value = Number(limitValue);
    if (!Number.isInteger(value) || value < 0) {
      setLimitError('Лимит должен быть целым неотрицательным числом');
      return;
    }

    setSavingLimit(true);
    setLimitError('');

    try {
      await updateMaxLoans(limitTarget.id, value);
      toast.success('Лимит обновлён');
      setLimitTarget(null);
      reload();
    } catch (limitSaveError) {
      setLimitError(getErrorMessage(limitSaveError, 'Не удалось обновить лимит'));
    } finally {
      setSavingLimit(false);
    }
  };

  const hasSearch = Boolean(search.trim());

  return (
    <div className="screen">
      <PageHeader
        title="Читатели"
        subtitle="Регистрация, блокировка и лимиты выдач"
        icon="readers"
        actions={
          <>
            <button type="button" className="retro-btn" onClick={reload}>
              <Icon name="refresh" size={13} /> Обновить
            </button>
            <button type="button" className="retro-btn primary" onClick={openCreate}>
              <Icon name="plus" size={13} /> Новый читатель
            </button>
          </>
        }
      />

      <div className="filter-bar">
        <div className="filter-field filter-field--grow">
          <span>Поиск читателя</span>
          <div className="search-field">
            <span className="search-icon"><Icon name="search" size={14} /></span>
            <input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(0);
              }}
              placeholder="ФИО, email или читательский билет…"
            />
          </div>
        </div>
        <div className="filter-actions">
          <button
            type="button"
            className="retro-btn"
            onClick={() => {
              setSearch('');
              setPage(0);
            }}
            disabled={!hasSearch}
          >
            Сбросить
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-state">Загрузка читателей…</div>
      ) : error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={reload}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : readers.length === 0 ? (
        <div className="empty-state">
          <strong>{hasSearch ? 'Читатели не найдены' : 'Читателей пока нет'}</strong>
          {hasSearch ? 'Измените поисковый запрос.' : 'Зарегистрируйте первого читателя.'}
        </div>
      ) : (
        <>
          <div className="table-wrap">
            <table className="retro-table">
              <thead>
                <tr>
                  <th>ФИО</th>
                  <th>Email</th>
                  <th>Билет</th>
                  <th>Роль</th>
                  <th>Статус</th>
                  <th>На руках</th>
                  <th>Лимит</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {readers.map((reader) => {
                  const busy = busyId === reader.id;

                  return (
                    <tr key={reader.id}>
                      <td className="title-cell">{reader.fullName}</td>
                      <td>{reader.email}</td>
                      <td className="nowrap">{reader.libraryCardNumber || '—'}</td>
                      <td>{userRoleLabel(reader.roleName)}</td>
                      <td>
                        <span className={`badge ${statusTone(reader.statusName)}`}>
                          {userStatusLabel(reader.statusName)}
                        </span>
                      </td>
                      <td className="num">{reader.activeLoansCount ?? 0}</td>
                      <td className="num">{reader.maxActiveLoans ?? '—'}</td>
                      <td>
                        <div className="row-actions">
                          {isAdmin ? (
                            <>
                              <button
                                type="button"
                                className={`retro-btn retro-btn--small ${reader.statusName === 'ACTIVE' ? 'danger' : 'success'}`}
                                onClick={() => handleStatus(reader)}
                                disabled={busy}
                              >
                                {reader.statusName === 'ACTIVE' ? 'Заблокировать' : 'Активировать'}
                              </button>
                              <button
                                type="button"
                                className="retro-btn retro-btn--small"
                                onClick={() => openLimit(reader)}
                                disabled={busy}
                              >
                                Лимит
                              </button>
                            </>
                          ) : (
                            <span className="hint">Только просмотр</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="catalog-footer">
            <span className="hint">
              {hasSearch ? `Найдено: ${totalElements}` : `Всего читателей: ${totalElements}`}
            </span>
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              onChange={setPage}
              disabled={hasSearch}
            />
          </div>
        </>
      )}

      {createOpen ? (
        <Dialog
          title="Регистрация читателя"
          icon="readers"
          onClose={closeCreate}
          footer={
            <>
              <button type="button" className="retro-btn" onClick={closeCreate} disabled={creating}>
                Отмена
              </button>
              <button type="button" className="retro-btn primary" onClick={saveReader} disabled={creating}>
                {creating ? 'Создание…' : 'Создать'}
              </button>
            </>
          }
        >
          {createError ? <div className="form-error">{createError}</div> : null}
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="reader-last">Фамилия <span className="required">*</span></label>
              <input
                id="reader-last"
                value={createForm.lastName}
                onChange={(event) => setCreateForm((current) => ({ ...current, lastName: event.target.value }))}
                autoFocus
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="reader-first">Имя <span className="required">*</span></label>
              <input
                id="reader-first"
                value={createForm.firstName}
                onChange={(event) => setCreateForm((current) => ({ ...current, firstName: event.target.value }))}
                required
              />
            </div>
            <div className="form-group span-2">
              <label htmlFor="reader-middle">Отчество</label>
              <input
                id="reader-middle"
                value={createForm.middleName}
                onChange={(event) => setCreateForm((current) => ({ ...current, middleName: event.target.value }))}
              />
            </div>
            <div className="form-group span-2">
              <label htmlFor="reader-email">Email <span className="required">*</span></label>
              <input
                id="reader-email"
                type="email"
                value={createForm.email}
                onChange={(event) => setCreateForm((current) => ({ ...current, email: event.target.value }))}
                required
              />
            </div>
          </div>
          <p className="hint">
            Номер читательского билета и лимит выдач будут назначены автоматически.
          </p>
        </Dialog>
      ) : null}

      {limitTarget ? (
        <Dialog
          title="Лимит выдач"
          icon="readers"
          onClose={() => setLimitTarget(null)}
          footer={
            <>
              <button type="button" className="retro-btn" onClick={() => setLimitTarget(null)} disabled={savingLimit}>
                Отмена
              </button>
              <button type="button" className="retro-btn primary" onClick={saveLimit} disabled={savingLimit}>
                {savingLimit ? 'Сохранение…' : 'Сохранить'}
              </button>
            </>
          }
        >
          <p className="hint">Читатель: <strong>{limitTarget.fullName}</strong></p>
          {limitError ? <div className="form-error">{limitError}</div> : null}
          <div className="form-group">
            <label htmlFor="reader-limit">Максимальное число активных выдач</label>
            <input
              id="reader-limit"
              type="number"
              min="0"
              step="1"
              value={limitValue}
              onChange={(event) => setLimitValue(event.target.value)}
              autoFocus
            />
          </div>
        </Dialog>
      ) : null}
    </div>
  );
};

export default ReadersScreen;
