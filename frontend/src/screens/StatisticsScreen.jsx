import { useState, useEffect } from 'react';
import { getPopularBooks } from '../api/statistics';

const StatisticsScreen = () => {
  const [books, setBooks] = useState([]);

useEffect(() => {
  getPopularBooks(10, null)
    .then(res => setBooks(res.data))
    .catch(() => {});
}, []);

  return (
    <div>
      <h2>Топ популярных книг</h2>
      <table>
        <thead><tr><th>Название</th><th>Авторы</th><th>Кол-во выдач</th></tr></thead>
        <tbody>
          {books.map((b, i) => (
            <tr key={i}>
              <td>{b.title}</td>
              <td>{b.authors}</td>
              <td>{b.loanCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default StatisticsScreen;