import { useState, useEffect } from 'react';
import { getBooks, deleteBook, searchBooks } from '../api/books';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const BooksScreen = () => {
  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();
  const { user } = useAuth();

  const fetchBooks = () => {
    const params = { page, size: 10 };
    if (search) params.title = search;
    searchBooks(params).then(res => {
      setBooks(res.data.content);
      setTotalPages(res.data.totalPages);
    }).catch(err => {
      console.error('Ошибка загрузки книг:', err);
    });
  };

  useEffect(() => { fetchBooks(); }, [page, search]);

  const handleDelete = async (id) => {
    if (window.confirm('Удалить книгу?')) {
      await deleteBook(id);
      fetchBooks();
    }
  };

  return (
    <div>
      <h2>Книги</h2>
      <div>
        <input placeholder="Поиск по названию" value={search} onChange={e => setSearch(e.target.value)} />
        <button onClick={fetchBooks}>Найти</button>
        {user?.role === 'ROLE_LIBRARIAN' || user?.role === 'ROLE_ADMIN' ? (
          <button onClick={() => navigate('/books/new')}>Добавить</button>
        ) : null}
      </div>
      <table>
        <thead><tr><th>Название</th><th>Авторы</th><th>Жанр</th><th></th></tr></thead>
        <tbody>
          {books.map(b => (
            <tr key={b.id}>
              <td>{b.title}</td>
              <td>{b.authors?.map(a => a.lastName).join(', ')}</td>
              <td>{b.genre}</td>
              <td>
                <button onClick={() => navigate(`/books/${b.id}`)}>Экземпляры</button>
                {user?.role === 'ROLE_ADMIN' && <button onClick={() => handleDelete(b.id)}>Удалить</button>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div>
        <button disabled={page === 0} onClick={() => setPage(p => p-1)}>Назад</button>
        <span> {page+1}/{totalPages} </span>
        <button disabled={page+1 >= totalPages} onClick={() => setPage(p => p+1)}>Вперед</button>
      </div>
    </div>
  );
};

export default BooksScreen;