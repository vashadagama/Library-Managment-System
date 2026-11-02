import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getBook } from '../api/books';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { genreLabel } from '../utils/labels';
import CopiesPanel from '../components/CopiesPanel';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const BookDetailScreen = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const canManage = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_LIBRARIAN';
  const canDelete = user?.role === 'ROLE_ADMIN';

  const loadBook = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await getBook(id);
      setBook(response.data);
    } catch (loadError) {
      setBook(null);
      setError(getErrorMessage(loadError, 'Не удалось загрузить книгу'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadBook();
  }, [loadBook, reloadKey]);

  if (loading) {
    return <div className="loading-state">Загрузка книги…</div>;
  }

  if (error || !book) {
    return (
      <div className="screen">
        <PageHeader
          title="Книга"
          icon="book"
          breadcrumbs={<Link to="/catalog?type=books">Каталог</Link>}
        />
        <div className="error-state">
          <strong>Книга не загружена</strong>
          {error || 'Книга не найдена'}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={loadBook}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
            <button type="button" className="retro-btn retro-btn--small" onClick={() => navigate('/catalog?type=books')}>
              <Icon name="arrowLeft" size={13} /> В каталог
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="screen">
      <PageHeader
        title={book.title}
        subtitle={book.authors?.map((author) => author.fullName).join(', ') || 'Авторы не указаны'}
        icon="book"
        breadcrumbs={
          <>
            <Link to="/catalog?type=books">Каталог</Link>
            <span>/</span>
            <span>{book.title}</span>
          </>
        }
        actions={
          <>
            <button type="button" className="retro-btn" onClick={() => navigate('/catalog?type=books')}>
              <Icon name="arrowLeft" size={13} /> Назад
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate(`/issue/${book.id}?type=books`)}>
              <Icon name="loans" size={13} /> Выдать
            </button>
            {canManage ? (
              <button type="button" className="retro-btn primary" onClick={() => navigate(`/books/edit/${book.id}`)}>
                <Icon name="edit" size={13} /> Изменить
              </button>
            ) : null}
          </>
        }
      />

      <section className="retro-panel panel">
        <div className="section-title">
          <span><Icon name="info" size={14} /> Сведения о книге</span>
        </div>
        <div className="meta-grid">
          <div className="meta-row">
            <span className="meta-row__label">Авторы</span>
            <span className="meta-row__value">{book.authors?.map((author) => author.fullName).join(', ') || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Издательство</span>
            <span className="meta-row__value">{book.publisherName || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Жанр</span>
            <span className="meta-row__value">{genreLabel(book.genreName)}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Язык</span>
            <span className="meta-row__value">{book.languageName || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Дата издания</span>
            <span className="meta-row__value">{formatDate(book.publicationDate)}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">ISBN</span>
            <span className="meta-row__value">{book.isbn || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Страниц</span>
            <span className="meta-row__value">{book.pageCount ?? '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Расположение</span>
            <span className="meta-row__value">{book.locationName || '—'}</span>
          </div>
        </div>
      </section>

      <CopiesPanel itemId={book.id} canManage={canManage} canDelete={canDelete} />
    </div>
  );
};

export default BookDetailScreen;
