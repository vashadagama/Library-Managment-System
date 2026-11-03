import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getAllActiveLoans, getOverdueLoans, renewLoan, returnBook } from '../api/loans';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatCount } from '../utils/format';
import { loanStatusLabel, statusTone } from '../utils/labels';
import ConfirmDialog from '../components/ConfirmDialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 15;

const LoansScreen = () => {
  const navigate = useNavigate();
  const [activeLoans, setActiveLoans] = useState([]);
  const [overdueLoans, setOverdueLoans] = useState([]);
  const [tab, setTab] = useState('active');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [busyId, setBusyId] = useState(null);
  const [returnTarget, setReturnTarget] = useState(null);
  const [returning, setReturning] = useState(false);

  const loadLoans = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [activeResponse, overdueResponse] = await Promise.all([
        getAllActiveLoans(),
        getOverdueLoans(),
      ]);
      setActiveLoans(Array.isArray(activeResponse.data) ? activeResponse.data : []);
      setOverdueLoans(Array.isArray(overdueResponse.data) ? overdueResponse.data : []);
    } catch (loadError) {
      setActiveLoans([]);
      setOverdueLoans([]);
      setError(getErrorMessage(loadError, 'Не удалось загрузить выдачи'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLoans();
  }, [loadLoans, reloadKey]);

  const reload = () => setReloadKey((key) => key + 1);

  const source = tab === 'overdue' ? overdueLoans : activeLoans;

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    const list = term
      ? source.filter((loan) =>
          [loan.bookTitle, loan.userFullName, loan.inventoryNumber]
            .filter(Boolean)
            .some((value) => value.toLowerCase().includes(term))
        )
      : source;

    return [...list].sort((left, right) => {
      const leftDate = left.dueDate || '';
      const rightDate = right.dueDate || '';
      return leftDate.localeCompare(rightDate);
    });
  }, [source, search]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const pageItems = filtered.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE);
  const overdueIds = useMemo(
    () => new Set(overdueLoans.map((loan) => loan.id)),
    [overdueLoans]
  );

  const changeTab = (nextTab) => {
    setTab(nextTab);
    setPage(0);
  };

  const handleRenew = async (loan) => {
    setBusyId(loan.id);

    try {
      await renewLoan(loan.id, 14);
      toast.success('Срок выдачи продлён на 14 дней');
      reload();
    } catch (renewError) {
      toast.error(getErrorMessage(renewError, 'Не удалось продлить выдачу'));
    } finally {
      setBusyId(null);
    }
  };

  const handleReturn = async () => {
    if (!returnTarget) return;

    setReturning(true);
    try {
      await returnBook(returnTarget.id);
      toast.success('Издание возвращено');
      setReturnTarget(null);
      reload();
    } catch (returnError) {
      toast.error(getErrorMessage(returnError, 'Не удалось вернуть издание'));
    } finally {
      setReturning(false);
    }
  };

  return (
    <div className="screen">
      <PageHeader
        title="Выдачи"
        subtitle="Активные и просроченные выдачи"
        icon="loans"
        actions={
          <>
            <button type="button" className="retro-btn" onClick={reload}>
              <Icon name="refresh" size={13} /> Обновить
            </button>
            <button type="button" className="retro-btn primary" onClick={() => navigate('/catalog')}>
              <Icon name="plus" size={13} /> Выдать издание
            </button>
          </>
        }
      />

      <div className="stats-grid stats-grid--compact">
        <div className="stat-card">
          <div className="stat-card__label">Активные выдачи</div>
          <div className="stat-card__value">{activeLoans.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__label">Просрочено</div>
          <div className={`stat-card__value${overdueLoans.length ? ' danger-text' : ''}`}>{overdueLoans.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__label">Показано</div>
          <div className="stat-card__value">{filtered.length}</div>
        </div>
      </div>

      <div className="filter-bar">
        <div className="filter-field">
          <span>Раздел</span>
          <div className="segmented">
            <button
              type="button"
              className={`retro-btn${tab === 'active' ? ' active' : ''}`}
              onClick={() => changeTab('active')}
            >
              Активные ({activeLoans.length})
            </button>
            <button
              type="button"
              className={`retro-btn${tab === 'overdue' ? ' active' : ''}`}
              onClick={() => changeTab('overdue')}
            >
              Просроченные ({overdueLoans.length})
            </button>
          </div>
        </div>

        <div className="filter-field filter-field--grow">
          <span>Поиск</span>
          <div className="search-field">
            <span className="search-icon"><Icon name="search" size={14} /></span>
            <input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(0);
              }}
              placeholder="Издание, читатель или инвентарный номер…"
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
            disabled={!search}
          >
            Сбросить
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-state">Загрузка выдач…</div>
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
      ) : pageItems.length === 0 ? (
        <div className="empty-state">
          <strong>
            {search
              ? 'Выдачи не найдены'
              : tab === 'overdue'
                ? 'Просроченных выдач нет'
                : 'Активных выдач нет'}
          </strong>
          {search ? 'Измените поисковый запрос.' : 'Все издания возвращены в срок.'}
        </div>
      ) : (
        <>
          <div className="table-wrap">
            <table className="retro-table">
              <thead>
                <tr>
                  <th>Читатель</th>
                  <th>Издание</th>
                  <th>Инв. №</th>
                  <th>Выдано</th>
                  <th>Срок</th>
                  <th>Статус</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((loan) => {
                  const isOverdue = overdueIds.has(loan.id) || loan.statusName === 'OVERDUE';
                  const busy = busyId === loan.id;

                  return (
                    <tr key={loan.id} className={isOverdue ? 'row-overdue' : undefined}>
                      <td className="title-cell">{loan.userFullName || '—'}</td>
                      <td>{loan.bookTitle || '—'}</td>
                      <td className="nowrap">{loan.inventoryNumber || '—'}</td>
                      <td className="nowrap">{formatDate(loan.loanDate)}</td>
                      <td className="nowrap">{formatDate(loan.dueDate)}</td>
                      <td>
                        <span className={`badge ${statusTone(isOverdue ? 'OVERDUE' : loan.statusName)}`}>
                          {loanStatusLabel(isOverdue ? 'OVERDUE' : loan.statusName)}
                        </span>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="retro-btn retro-btn--small success"
                            onClick={() => setReturnTarget(loan)}
                            disabled={busy}
                          >
                            <Icon name="check" size={12} /> Вернуть
                          </button>
                          <button
                            type="button"
                            className="retro-btn retro-btn--small"
                            onClick={() => handleRenew(loan)}
                            disabled={busy}
                          >
                            <Icon name="refresh" size={12} /> Продлить
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="catalog-footer">
            <span className="hint">{formatCount(filtered.length, 'выдача', 'выдачи', 'выдач')}</span>
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={filtered.length}
              onChange={setPage}
            />
          </div>
        </>
      )}

      {returnTarget ? (
        <ConfirmDialog
          title="Возврат издания"
          message={`Подтвердить возврат «${returnTarget.bookTitle}» от ${returnTarget.userFullName || 'читателя'}?`}
          confirmLabel="Вернуть"
          busy={returning}
          onConfirm={handleReturn}
          onCancel={() => setReturnTarget(null)}
        />
      ) : null}
    </div>
  );
};

export default LoansScreen;
