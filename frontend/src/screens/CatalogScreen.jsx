import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getBooks, searchBooks, deleteBook } from '../api/books';
import { getMagazines, deleteMagazine } from '../api/magazines';
import { useAuth } from '../context/AuthContext';

const CatalogScreen = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();

  const initialType = searchParams.get('type') || 'books';
  const initialSearch = searchParams.get('search') || '';

  const [type, setType] = useState(initialType);
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState(initialSearch);
  const [loading, setLoading] = useState(false);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10 };
      if (search) params.title = search;

      let res;
      if (type === 'books') {
        res = await searchBooks(params);
      } else {
        // для журналов используем getMagazines с параметром title
        res = await getMagazines(page, 10, { title: search });
      }
      setItems(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [type, page, search]);

  const handleTypeChange = (newType) => {
    setType(newType);
    setPage(0);
    // Оставляем поиск, но можно и сбросить по желанию
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 100px)' }}>
      <h2>📚 Каталог</h2>

      <div className="search-bar" style={{ flexWrap: 'wrap', gap: '10px' }}>
        <select value={type} onChange={(e) => handleTypeChange(e.target.value)}>
          <option value="books">Книги</option>
          <option value="magazines">Журналы</option>
        </select>
        <input
          placeholder={type === 'books' ? 'Поиск по названию книги...' : 'Поиск по названию журнала...'}
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        />
        { (user?.role === 'ROLE_LIBRARIAN' || user?.role === 'ROLE_ADMIN') && (
          <button onClick={() => navigate(type === 'books' ? '/books/new' : '/magazines/new')}>
            Добавить
          </button>
        )}
      </div>

      <div style={{ flex: 1, overflowY: 'auto', marginBottom: '80px' }}>
        <table>
          <thead>
            <tr>
              <th>Название</th>
              <th>Авторы / Издатель</th>
              <th>Жанр</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {items.map(item => (
              <tr key={item.id}>
                <td>{item.title}</td>
                <td>
                  {type === 'books'
                    ? (item.authors?.map(a => `${a.lastName} ${a.firstName}`).join(', ') || '—')
                    : (item.publisher || '—')}
                </td>
                <td>{item.genre}</td>
                <td style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                  <button onClick={() => navigate(`/${type}/${item.id}`)}>
                    Подробнее
                  </button>
                  <button onClick={() => navigate(`/issue/${item.id}?type=${type}`)}>
                    Выдать
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p>Загрузка...</p>}
      </div>

      <div style={{
        position: 'sticky',
        bottom: 0,
        background: 'var(--bg)',
        padding: '10px 0',
        borderTop: '1px solid var(--border)',
        display: 'flex',
        justifyContent: 'center',
        gap: '10px',
        zIndex: 10
      }}>
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Назад</button>
        <span style={{ alignSelf: 'center' }}>Страница {page + 1} из {totalPages}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(p => p + 1)}>Вперёд →</button>
      </div>
    </div>
  );
};

export default CatalogScreen;