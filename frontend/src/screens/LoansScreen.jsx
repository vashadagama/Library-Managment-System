import { useState, useEffect } from 'react';
import { getOverdueLoans, renewLoan, returnBook, getActiveLoansByUser } from '../api/loans';

const LoansScreen = () => {
  const [overdue, setOverdue] = useState([]);
  const [active, setActive] = useState([]);
  const [userId, setUserId] = useState('');

  const fetchData = () => {
    getOverdueLoans().then(res => setOverdue(res.data)).catch(() => {});
    if (userId.trim()) {
      getActiveLoansByUser(userId).then(res => setActive(res.data)).catch(() => setActive([]));
    } else {
      setActive([]);
    }
  };

  useEffect(() => { fetchData(); }, [userId]);

  const handleReturn = async (loanId) => {
    await returnBook(loanId);
    fetchData();
  };

  const handleRenew = async (loanId) => {
    await renewLoan(loanId, 14);
    alert('Продлено');
    fetchData();
  };

  return (
    <div>
      <h2>📖 Управление выдачами</h2>

      <div className="card">
        <h3>Поиск активных выдач читателя</h3>
        <div className="search-bar">
          <input placeholder="ID читателя" value={userId} onChange={e => setUserId(e.target.value)} />
          <button onClick={fetchData}>Показать</button>
        </div>
        {active.length > 0 && (
          <table>
            <thead><tr><th>Книга</th><th>Дата выдачи</th><th>До какого числа</th><th></th></tr></thead>
            <tbody>
              {active.map(loan => (
                <tr key={loan.id}>
                  <td>{loan.bookTitle}</td>
                  <td>{loan.loanDate}</td>
                  <td className={loan.status === 'OVERDUE' ? 'badge-danger' : ''}>{loan.dueDate}</td>
                  <td>
                    <button onClick={() => handleReturn(loan.id)}>Вернуть</button>
                    <button onClick={() => handleRenew(loan.id)}>Продлить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h3>⚠️ Просроченные выдачи (все)</h3>
        {overdue.length === 0 ? (
          <p style={{ color: 'var(--success)' }}>✅ Нет просроченных выдач</p>
        ) : (
          <table>
            <thead><tr><th>Книга</th><th>Читатель</th><th>Срок до</th><th></th></tr></thead>
            <tbody>
              {overdue.map(loan => (
                <tr key={loan.id}>
                  <td>{loan.bookTitle}</td>
                  <td>{loan.userFullName}</td>
                  <td>{loan.dueDate}</td>
                  <td><button onClick={() => handleReturn(loan.id)}>Вернуть</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default LoansScreen;