import { useState, useEffect } from 'react';
import { getSummaryStatistics, getOverdueLoans } from '../api/loans';
import { searchBooks } from '../api/books';
import { getReaders } from '../api/readers';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [stats, setStats] = useState({});
  const [overdue, setOverdue] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('book');
  const navigate = useNavigate();

  useEffect(() => {
    getSummaryStatistics().then(res => setStats(res.data));
    getOverdueLoans().then(res => setOverdue(res.data));
  }, []);

  const handleQuickSearch = () => {
    if (searchType === 'book') navigate(`/books?search=${searchTerm}`);
    else navigate(`/readers?search=${searchTerm}`);
  };

  return (
    <div>
      <h2>Главная</h2>
      <div style={{ display: 'flex', gap: 20 }}>
        <div>Всего выдач: {stats.totalLoans}</div>
        <div>Активных: {stats.activeLoans}</div>
        <div>Просрочек: {stats.overdueReturns}</div>
      </div>

      <div style={{ marginTop: 20 }}>
        <h3>Быстрый поиск</h3>
        <select value={searchType} onChange={e => setSearchType(e.target.value)}>
          <option value="book">Книги</option>
          <option value="reader">Читатели</option>
        </select>
        <input value={searchTerm} onChange={e => setSearchTerm(e.target.value)} />
        <button onClick={handleQuickSearch}>Искать</button>
      </div>

      <div style={{ marginTop: 20 }}>
        <h3>Просроченные выдачи</h3>
        {overdue.length === 0 ? <p>Нет просрочек</p> : (
          <table><tbody>
            {overdue.map(l => (
              <tr key={l.id}>
                <td>{l.bookTitle}</td>
                <td>{l.userFullName}</td>
                <td>{l.dueDate}</td>
              </tr>
            ))}
          </tbody></table>
        )}
      </div>
    </div>
  );
};

export default Dashboard;