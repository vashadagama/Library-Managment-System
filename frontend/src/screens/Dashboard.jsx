import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSummaryStatistics, getOverdueLoans } from '../api/statistics';
import { searchBooks } from '../api/books';

const Dashboard = () => {
  const [stats, setStats] = useState({});
  const [overdue, setOverdue] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('book');
  const [fastResults, setFastResults] = useState([]);
  const [loadingFast, setLoadingFast] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getSummaryStatistics().then(res => setStats(res.data)).catch(() => {});
    getOverdueLoans().then(res => setOverdue(res.data.slice(0, 5))).catch(() => {});
  }, []);

  // debounced search
  useEffect(() => {
    if (searchTerm.trim().length < 2) {
      setFastResults([]);
      return;
    }
    const timer = setTimeout(() => {
      setLoadingFast(true);
      const params = { title: searchTerm.trim(), size: 3, page: 0 };
      searchBooks(params)
        .then(res => setFastResults(res.data.content))
        .catch(() => setFastResults([]))
        .finally(() => setLoadingFast(false));
    }, 400);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const handleShowAll = () => {
    const query = encodeURIComponent(searchTerm.trim());
    if (searchType === 'book') {
      navigate(`/catalog?type=books&search=${query}`);
    } else if (searchType === 'magazine') {
      navigate(`/catalog?type=magazines&search=${query}`);
    } else {
      navigate(`/readers?search=${query}`);
    }
  };

  return (
    <div>
      <h2>📋 Главная панель</h2>

      <div className="grid grid-3" style={{ marginBottom: '30px' }}>
        <div className="stats-card">
          <h3>Всего выдач</h3>
          <div className="number">{stats.totalLoans || 0}</div>
        </div>
        <div className="stats-card" style={{ background: 'linear-gradient(135deg, #27ae60, #2ecc71)' }}>
          <h3>Активных</h3>
          <div className="number">{stats.activeLoans || 0}</div>
        </div>
        <div className="stats-card" style={{ background: 'linear-gradient(135deg, #e74c3c, #c0392b)' }}>
          <h3>Просрочек</h3>
          <div className="number">{stats.overdueReturns || 0}</div>
        </div>
      </div>

      <div className="card">
        <h3>🔍 Быстрый поиск</h3>
        <div className="search-bar">
          <select value={searchType} onChange={e => { setSearchType(e.target.value); setFastResults([]); setSearchTerm(''); }}>
            <option value="book">📚 Книги</option>
            <option value="magazine">📰 Журналы</option>
            <option value="reader">👤 Читатели</option>
          </select>
          <input
            placeholder={
              searchType === 'book' ? 'Название книги...' :
              searchType === 'magazine' ? 'Название журнала...' :
              'Фамилия или email...'
            }
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
          />
        </div>

        {/* Показываем топ-3 результата */}
        {searchTerm.trim().length >= 2 && (
          <div style={{ marginTop: '12px' }}>
            {loadingFast && <p>Загрузка...</p>}
            {!loadingFast && fastResults.length > 0 && (
              <>
                <table style={{ marginBottom: '8px' }}>
                  <thead>
                    <tr>
                      <th>Название</th>
                      <th>Автор / Издатель</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fastResults.map(item => (
                      <tr key={item.id}>
                        <td>{item.title}</td>
                        <td>
                          {searchType === 'book'
                            ? (item.authors?.map(a => `${a.lastName} ${a.firstName}`).join(', ') || '—')
                            : (item.publisher || '—')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <button onClick={handleShowAll} style={{ background: 'var(--accent)', color: 'white', marginTop: '8px' }}>
                  Показать все результаты →
                </button>
              </>
            )}
          </div>
        )}
      </div>

      <div className="card">
        <h3>⚠️ Просроченные выдачи (последние 5)</h3>
        {overdue.length === 0 ? (
          <p style={{ color: 'var(--success)' }}>✅ Нет просроченных выдач</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Книга</th>
                <th>Читатель</th>
                <th>Срок до</th>
              </tr>
            </thead>
            <tbody>
              {overdue.map(l => (
                <tr key={l.id}>
                  <td>📖 {l.bookTitle}</td>
                  <td>👤 {l.userFullName}</td>
                  <td><span className="badge badge-danger">{l.dueDate}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default Dashboard;