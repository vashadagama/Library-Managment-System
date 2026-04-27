import { useState, useEffect } from 'react';
import { getOverdueLoans, renewLoan, returnBook } from '../api/loans';

const LoansScreen = () => {
  const [loans, setLoans] = useState([]);

  useEffect(() => { getOverdueLoans().then(res => setLoans(res.data)); }, []);

  const handleReturn = async (loanId) => {
    await returnBook(loanId);
    setLoans(prev => prev.filter(l => l.id !== loanId));
  };

  const handleRenew = async (loanId) => {
    await renewLoan(loanId, 14);
    alert('Продлено');
    // обновить список
  };

  return (
    <div>
      <h2>Просроченные выдачи</h2>
      <table>
        <thead><tr><th>Книга</th><th>Читатель</th><th>Дата возврата</th><th></th></tr></thead>
        <tbody>
          {loans.map(l => (
            <tr key={l.id}>
              <td>{l.bookTitle}</td>
              <td>{l.userFullName}</td>
              <td>{l.dueDate}</td>
              <td>
                <button onClick={() => handleReturn(l.id)}>Вернуть</button>
                <button onClick={() => handleRenew(l.id)}>Продлить</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default LoansScreen;