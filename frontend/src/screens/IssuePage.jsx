import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { getBook, getBookCopies } from '../api/books';
import { getMagazine } from '../api/magazines';
import { issueByLibraryItem } from '../api/loans';

const IssuePage = () => {
  const { itemId } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') || 'books';
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [userId, setUserId] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchItem = async () => {
      try {
        if (type === 'books') {
          const res = await getBook(itemId);
          setItem(res.data);
        } else {
          const res = await getMagazine(itemId);
          setItem(res.data);
        }
      } catch (err) {
        console.error('Ошибка загрузки издания:', err);
      }
    };
    fetchItem();
  }, [itemId, type]);

  const handleIssue = async () => {
    if (!userId.trim()) {
      setMessage('Введите ID читателя');
      return;
    }
    try {
      await issueByLibraryItem(userId.trim(), itemId);
      setMessage('✅ Издание успешно выдано!');
      setUserId('');
      // через 2 секунды можно вернуться на каталог
      setTimeout(() => navigate(-1), 2000);
    } catch (err) {
      setMessage('❌ Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  if (!item) return <div>Загрузка...</div>;

  return (
    <div>
      <h2>Выдача издания</h2>
      <div className="card">
        <h3>{item.title}</h3>
        <p>
          {type === 'books'
            ? (item.authors?.map(a => `${a.lastName} ${a.firstName}`).join(', ') || 'Авторы не указаны')
            : `Издатель: ${item.publisher || 'не указан'}`
          }
        </p>
        <p><strong>Тип:</strong> {type === 'books' ? 'Книга' : 'Журнал'}</p>
        <p><strong>Жанр:</strong> {item.genre}</p>
      </div>

      <div className="card" style={{ marginTop: '20px' }}>
        <div className="form-group">
          <label>ID читателя</label>
          <input
            type="text"
            placeholder="Введите ID читателя"
            value={userId}
            onChange={e => setUserId(e.target.value)}
          />
        </div>
        <button onClick={handleIssue} style={{ marginTop: '10px' }}>Выдать</button>
        {message && <p style={{ marginTop: '10px' }}>{message}</p>}
        <p style={{ marginTop: '10px', fontSize: '12px', color: 'gray' }}>
          ID читателя можно найти на странице «Читатели».
        </p>
      </div>
    </div>
  );
};

export default IssuePage;