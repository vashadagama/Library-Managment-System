import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getOverdueLoans, getPopularBooks, getSummaryStatistics } from '../api/statistics';
import { getErrorMessage } from '../utils/errors';
import { formatCount } from '../utils/format';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const currentYear = new Date().getFullYear();
const YEAR_OPTIONS = Array.from({ length: 7 }, (_, index) => currentYear - index);

const StatisticsScreen = () => {
  const [summary, setSummary] = useState({});
  const [overdueNow, setOverdueNow] = useState(0);
  const [books, setBooks] = useState([]);
  const [year, setYear] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const loadStatistics = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [summaryResponse, booksResponse, overdueResponse] = await Promise.all([
        getSummaryStatistics(),
        getPopularBooks(10, year || null),
        getOverdueLoans(),
      ]);

      setSummary(summaryResponse.data || {});
      setBooks(Array.isArray(booksResponse.data) ? booksResponse.data : []);
      setOverdueNow(Array.isArray(overdueResponse.data) ? overdueResponse.data.length : 0);
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить статистику'));
      setBooks([]);
    } finally {
      setLoading(false);
    }
  }, [year]);

  useEffect(() => {
    loadStatistics();
  }, [loadStatistics, reloadKey]);

  const maxLoans = useMemo(
    () => books.reduce((max, book) => Math.max(max, Number(book.loanCount) || 0), 0),
    [books]
  );

  const totalLoans = Number(summary.totalLoans) || 0;
  const activeLoans = Number(summary.activeLoans) || 0;
  const overdueReturns = Number(summary.overdueReturns) || 0;
  const returnRate = totalLoans > 0 ? Math.round(((totalLoans - activeLoans - overdueReturns) / totalLoans) * 100) : 0;

  return (
    <div className="screen">
      <PageHeader
        title="Статистика"
        subtitle="Показатели фонда и популярность изданий"
        icon="statistics"
        actions={
          <button type="button" className="retro-btn" onClick={() => setReloadKey((key) => key + 1)}>
            <Icon name="refresh" size={13} /> Обновить
          </button>
        }
      />

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-card__icon"><Icon name="loans" size={20} /></div>
          <div className="stat-card__label">Всего выдач</div>
          <div className="stat-card__value">{totalLoans}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon"><Icon name="book" size={20} /></div>
          <div className="stat-card__label">Активные выдачи</div>
          <div className="stat-card__value">{activeLoans}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon"><Icon name="warning" size={20} /></div>
          <div className="stat-card__label">Просрочено сейчас</div>
          <div className={`stat-card__value${overdueNow > 0 ? ' danger-text' : ''}`}>{overdueNow}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon"><Icon name="statistics" size={20} /></div>
          <div className="stat-card__label">Возвращено вовремя</div>
          <div className="stat-card__value">{totalLoans > 0 ? `${Math.max(returnRate, 0)}%` : '—'}</div>
        </div>
      </div>

      <section className="retro-panel panel">
        <div className="section-title section-title--row">
          <span><Icon name="statistics" size={14} /> Самые популярные книги</span>
          <label className="inline-field" htmlFor="stats-year">
            <span>Год:</span>
            <select
              id="stats-year"
              value={year}
              onChange={(event) => setYear(event.target.value)}
            >
              <option value="">За всё время</option>
              {YEAR_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </label>
        </div>

        {loading ? (
          <div className="loading-state">Загрузка статистики…</div>
        ) : error ? (
          <div className="error-state">
            <strong>Ошибка загрузки</strong>
            {error}
            <div className="form-actions">
              <button type="button" className="retro-btn retro-btn--small" onClick={loadStatistics}>
                <Icon name="refresh" size={13} /> Повторить
              </button>
            </div>
          </div>
        ) : books.length === 0 ? (
          <div className="empty-state">
            <strong>Нет данных о выдачах</strong>
            {year ? 'За выбранный год книг не выдавали.' : 'Статистика появится после первых выдач.'}
          </div>
        ) : (
          <div className="table-wrap">
            <table className="retro-table">
              <thead>
                <tr>
                  <th className="rank-col">#</th>
                  <th>Название</th>
                  <th>Авторы</th>
                  <th className="popularity-col">Выдач</th>
                </tr>
              </thead>
              <tbody>
                {books.map((book, index) => {
                  const count = Number(book.loanCount) || 0;
                  const width = maxLoans > 0 ? Math.max((count / maxLoans) * 100, 4) : 0;

                  return (
                    <tr key={book.libraryItemId || `${book.title}-${index}`}>
                      <td className="rank-col">{index + 1}</td>
                      <td className="title-cell">
                        {book.libraryItemId ? (
                          <Link to={`/books/${book.libraryItemId}`}>{book.title}</Link>
                        ) : (
                          book.title || '—'
                        )}
                      </td>
                      <td>{book.authors || '—'}</td>
                      <td>
                        <div className="popularity">
                          <div className="popularity__bar">
                            <span style={{ width: `${width}%` }} />
                          </div>
                          <span className="popularity__value">{formatCount(count, 'выдача', 'выдачи', 'выдач')}</span>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default StatisticsScreen;
