import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getOverdueLoans, getSummaryStatistics } from '../api/loans';
import { searchBooks } from '../api/books';
import { getMagazines } from '../api/magazines';
import { searchReaders } from '../api/readers';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { genreLabel, loanStatusLabel, statusTone } from '../utils/labels';
import useDebouncedValue from '../hooks/useDebouncedValue';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const SEARCH_TYPES = {
  book: { label: 'Книги', placeholder: 'Название книги…' },
  magazine: { label: 'Журналы', placeholder: 'Название журнала…' },
  reader: { label: 'Читатели', placeholder: 'Фамилия, имя или email…' },
};

const Dashboard = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState({ totalLoans: 0, activeLoans: 0, overdueReturns: 0 });
  const [overdue, setOverdue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchType, setSearchType] = useState('book');
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');
  const debouncedQuery = useDebouncedValue(query, 350);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [statsResponse, overdueResponse] = await Promise.all([
        getSummaryStatistics(),
        getOverdueLoans(),
      ]);
      setStats(statsResponse.data || {});
      setOverdue(Array.isArray(overdueResponse.data) ? overdueResponse.data : []);
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить данные главной панели'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    const term = debouncedQuery.trim();

    if (term.length < 2) {
      setResults([]);
      setSearchError('');
      setSearching(false);
      return undefined;
    }

    let ignore = false;
    setSearching(true);
    setSearchError('');

    const request =
      searchType === 'book'
        ? searchBooks({ title: term, page: 0, size: 5 })
        : searchType === 'magazine'
          ? getMagazines(0, 5, { title: term })
          : searchReaders(term);

    request
      .then((response) => {
        if (ignore) return;
        const list = Array.isArray(response.data)
          ? response.data
          : response.data?.content || [];
        setResults(list);
      })
      .catch((searchRequestError) => {
        if (ignore) return;
        setResults([]);
        setSearchError(getErrorMessage(searchRequestError, 'Поиск не выполнен'));
      })
      .finally(() => {
        if (!ignore) setSearching(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedQuery, searchType]);

  const showAll = () => {
    const term = query.trim();
    if (term.length < 2) return;

    if (searchType === 'reader') {
      navigate(`/readers?search=${encodeURIComponent(term)}`);
      return;
    }

    navigate(`/catalog?type=${searchType === 'book' ? 'books' : 'magazines'}&search=${encodeURIComponent(term)}`);
  };

  const openResult = (item) => {
    if (searchType === 'book') navigate(`/books/${item.id}`);
    else if (searchType === 'magazine') navigate(`/magazines/${item.id}`);
    else navigate(`/readers?search=${encodeURIComponent(item.fullName || item.email || '')}`);
  };

  const currentSearch = SEARCH_TYPES[searchType];
  const hasQuery = query.trim().length >= 2;

  return (
    <div className="screen">
      <PageHeader
        title="Главная панель"
        subtitle="Сводка по фонду, читателям и выдаче"
        icon="home"
        actions={
          <div className="quick-actions">
            <button type="button" className="retro-btn" onClick={() => navigate('/books/new')}>
              <Icon name="plus" size={13} /> Книга
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate('/magazines/new')}>
              <Icon name="plus" size={13} /> Журнал
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate('/catalog')}>
              <Icon name="loans" size={13} /> Выдать
            </button>
            <button
              type="button"
              className="retro-btn retro-btn--icon"
              onClick={loadDashboard}
              title="Обновить данные"
            >
              <Icon name="refresh" size={14} />
            </button>
          </div>
        }
      />

      {error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={loadDashboard}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : null}

      <div className="stats-grid">
        <div className="stat-card stat-card--blue">
          <div className="stat-card__label">Всего выдач</div>
          <div className="stat-card__value">{loading ? '…' : stats.totalLoans ?? 0}</div>
          <div className="stat-card__hint">за всё время</div>
        </div>
        <div className="stat-card stat-card--green">
          <div className="stat-card__label">Активных</div>
          <div className="stat-card__value">{loading ? '…' : stats.activeLoans ?? 0}</div>
          <div className="stat-card__hint">на руках сейчас</div>
        </div>
        <div className="stat-card stat-card--red">
          <div className="stat-card__label">Просрочено сейчас</div>
          <div className="stat-card__value">{loading ? '…' : overdue.length}</div>
          <div className="stat-card__hint">требуют внимания</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__label">Возвратов с опозданием</div>
          <div className="stat-card__value">{loading ? '…' : stats.overdueReturns ?? 0}</div>
          <div className="stat-card__hint">историческая статистика</div>
        </div>
      </div>

      <section className="retro-panel panel">
        <div className="section-title">
          <span><Icon name="search" size={14} /> Быстрый поиск</span>
        </div>

        <div className="filter-bar">
          <div className="segmented">
            {Object.entries(SEARCH_TYPES).map(([value, config]) => (
              <button
                key={value}
                type="button"
                className={`retro-btn${searchType === value ? ' active' : ''}`}
                onClick={() => {
                  setSearchType(value);
                  setResults([]);
                  setSearchError('');
                }}
              >
                {config.label}
              </button>
            ))}
          </div>
          <div className="search-field">
            <span className="search-icon"><Icon name="search" size={14} /></span>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={currentSearch.placeholder}
            />
          </div>
          <button
            type="button"
            className="retro-btn"
            onClick={showAll}
            disabled={!hasQuery || searching}
          >
            Показать все
          </button>
        </div>

        {searching ? <div className="loading-state">Поиск…</div> : null}

        {!searching && searchError ? (
          <div className="error-state">
            <strong>Поиск не выполнен</strong>
            {searchError}
          </div>
        ) : null}

        {!searching && !searchError && hasQuery && results.length === 0 ? (
          <div className="empty-state">Ничего не найдено. Попробуйте изменить запрос.</div>
        ) : null}

        {!searching && results.length > 0 ? (
          <div className="quick-search">
            <div className="quick-search__results">
              <table className="retro-table">
                <thead>
                  <tr>
                    {searchType === 'reader' ? (
                      <>
                        <th>ФИО</th>
                        <th>Email</th>
                        <th>Билет</th>
                        <th>На руках</th>
                      </>
                    ) : searchType === 'book' ? (
                      <>
                        <th>Название</th>
                        <th>Авторы</th>
                        <th>Жанр</th>
                      </>
                    ) : (
                      <>
                        <th>Название</th>
                        <th>Издательство</th>
                        <th>Жанр</th>
                      </>
                    )}
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {results.map((item) => (
                    <tr key={item.id} onClick={() => openResult(item)} style={{ cursor: 'pointer' }}>
                      {searchType === 'reader' ? (
                        <>
                          <td className="title-cell">{item.fullName}</td>
                          <td>{item.email}</td>
                          <td>{item.libraryCardNumber || '—'}</td>
                          <td className="num">{item.activeLoansCount ?? 0}</td>
                        </>
                      ) : searchType === 'book' ? (
                        <>
                          <td className="title-cell">{item.title}</td>
                          <td>{item.authors?.map((author) => author.lastName).join(', ') || '—'}</td>
                          <td>{item.genreName ? genreLabel(item.genreName) : '—'}</td>
                        </>
                      ) : (
                        <>
                          <td className="title-cell">{item.title}</td>
                          <td>{item.publisherName || '—'}</td>
                          <td>{item.genreName ? genreLabel(item.genreName) : '—'}</td>
                        </>
                      )}
                      <td className="nowrap">
                        <button type="button" className="retro-btn retro-btn--small" onClick={() => openResult(item)}>
                          Открыть
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="quick-search__footer">
              <button type="button" className="retro-btn retro-btn--small" onClick={showAll}>
                Показать все результаты <Icon name="arrowRight" size={12} />
              </button>
            </div>
          </div>
        ) : null}
      </section>

      <section className="retro-panel panel">
        <div className="section-title">
          <span><Icon name="warning" size={14} /> Просроченные выдачи</span>
          <span className="section-title__meta">
            <button type="button" className="retro-btn retro-btn--small" onClick={() => navigate('/loans')}>
              Все выдачи
            </button>
          </span>
        </div>

        {loading ? (
          <div className="loading-state">Загрузка…</div>
        ) : overdue.length === 0 ? (
          <div className="empty-state">
            <strong>Просроченных выдач нет</strong>
            Все читатели возвращают книги вовремя.
          </div>
        ) : (
          <div className="table-wrap">
            <table className="retro-table">
              <thead>
                <tr>
                  <th>Издание</th>
                  <th>Читатель</th>
                  <th>Инв. номер</th>
                  <th>Срок до</th>
                  <th>Статус</th>
                </tr>
              </thead>
              <tbody>
                {overdue.slice(0, 5).map((loan) => (
                  <tr key={loan.id}>
                    <td className="title-cell">{loan.bookTitle}</td>
                    <td>{loan.userFullName}</td>
                    <td>{loan.inventoryNumber}</td>
                    <td className="nowrap">{formatDate(loan.dueDate)}</td>
                    <td>
                      <span className={`badge ${statusTone(loan.statusName)}`}>
                        {loanStatusLabel(loan.statusName)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default Dashboard;
