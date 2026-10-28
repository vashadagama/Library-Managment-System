import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { getBook } from '../api/books';
import { getMagazine } from '../api/magazines';
import { issueByLibraryItem } from '../api/loans';
import { toast } from 'react-toastify';

const IssuePage = () => {
  const { itemId } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') || 'books';
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [userId, setUserId] = useState('');

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
        toast.error('Ошибка загрузки издания');
      }
    };
    fetchItem();
  }, [itemId, type]);

  const handleIssue = async () => {
    if (!userId.trim()) {
      toast.warning('Введите ID читателя');
      return;
    }
    try {
      await issueByLibraryItem(userId.trim(), itemId);
      toast.success('Издание успешно выдано!');
      setUserId('');
      setTimeout(() => navigate(-1), 2000);
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
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
        <p style={{ marginTop: '10px', fontSize: '12px', color: 'gray' }}>
          ID читателя можно найти на странице «Читатели».
        </p>
      </div>
    </div>
  );
};

export default IssuePage;