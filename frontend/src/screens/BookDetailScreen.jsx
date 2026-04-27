import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getBook, getBookCopies, addCopy, updateCopyStatus } from '../api/books';
import { issueByCopy } from '../api/loans';

const BookDetailScreen = () => {
  const { id } = useParams();
  const [book, setBook] = useState(null);
  const [copies, setCopies] = useState([]);
  const [invNumber, setInvNumber] = useState('');
  const [userId, setUserId] = useState('');

  const fetchData = () => {
    getBook(id).then(res => setBook(res.data));
    getBookCopies(id).then(res => setCopies(res.data));
  };

  useEffect(() => { fetchData(); }, [id]);

  const handleAddCopy = async () => {
    if (!invNumber.trim()) {
      alert('Введите инвентарный номер');
      return;
    }
    try {
      await addCopy({ libraryItemId: id, inventoryNumber: invNumber.trim() });
      setInvNumber('');
      fetchData();
    } catch (err) {
      alert('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleStatusChange = async (copyId, status) => {
    await updateCopyStatus(copyId, status);
    fetchData();
  };

  const handleIssue = async (copyId) => {
    if (!userId.trim()) {
      alert('Введите ID читателя');
      return;
    }
    try {
      await issueByCopy(userId.trim(), copyId);
      alert('Книга выдана');
      fetchData();
    } catch (err) {
      alert('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  if (!book) return <div>Загрузка...</div>;

  return (
    <div>
      <h2>{book.title}</h2>
      <p>{book.authors?.map(a => `${a.firstName} ${a.lastName}`).join(', ')}</p>
      <p>ISBN: {book.isbn} | Жанр: {book.genre}</p>

      <h3>Экземпляры</h3>
      <div>
        <input placeholder="Инв. номер" value={invNumber} onChange={e => setInvNumber(e.target.value)} />
        <button onClick={handleAddCopy}>Добавить экземпляр</button>
      </div>
      <table>
        <thead><tr><th>Инв.номер</th><th>Статус</th><th>Действия</th></tr></thead>
        <tbody>
          {copies.map(c => (
            <tr key={c.id}>
              <td>{c.inventoryNumber}</td>
              <td>{c.status}</td>
              <td>
                <select onChange={(e) => handleStatusChange(c.id, e.target.value)} defaultValue="">
                  <option value="" disabled>Изменить статус</option>
                  <option value="AVAILABLE">Доступно</option>
                  <option value="UNDER_REPAIR">На ремонте</option>
                  <option value="LOST">Утеряно</option>
                </select>
                {c.status === 'AVAILABLE' && (
                  <div>
                    <input placeholder="ID читателя" value={userId} onChange={e => setUserId(e.target.value)} />
                    <button onClick={() => handleIssue(c.id)}>Выдать</button>
                  </div>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default BookDetailScreen;