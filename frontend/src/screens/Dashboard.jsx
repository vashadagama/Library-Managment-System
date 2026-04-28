import { useState, useEffect } from 'react';
import { getSummaryStatistics, getOverdueLoans } from '../api/statistics';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [stats, setStats] = useState({});
  const [overdue, setOverdue] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('book');
  const navigate = useNavigate();

  useEffect(() => {
    getSummaryStatistics().then(res => setStats(res.data)).catch(() => {});
    getOverdueLoans().then(res => setOverdue(res.data.slice(0, 5))).catch(() => {});
  }, []);

  const handleQuickSearch = () => {
    if (!searchTerm.trim()) return;
    switch (searchType) {
      case 'book':
        navigate(`/catalog?type=books&search=${encodeURIComponent(searchTerm)}`);
        break;
      case 'magazine':
        navigate(`/catalog?type=magazines&search=${encodeURIComponent(searchTerm)}`);
        break;
      case 'reader':
        navigate(`/readers?search=${encodeURIComponent(searchTerm)}`);
        break;
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
          <select value={searchType} onChange={e => setSearchType(e.target.value)}>
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
            onKeyPress={e => e.key === 'Enter' && handleQuickSearch()}
          />
          <button onClick={handleQuickSearch}>Искать</button>
        </div>
        {searchTerm.trim() && (
          <div style={{ marginTop: '8px' }}>
            <button
              onClick={handleQuickSearch}
              style={{ background: 'var(--accent)', color: 'white' }}
            >
              Перейти ко всем результатам →
            </button>
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