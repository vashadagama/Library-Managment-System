import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { createBook, getBook, updateBook } from '../api/books';
import { getAuthors } from '../api/authors';
import { getGenres, getLanguages, getLocations, getPublishers } from '../api/dictionaries';
import { getErrorMessage } from '../utils/errors';
import { genreLabel } from '../utils/labels';
import Icon from './Icon';
import PageHeader from './PageHeader';

const emptyForm = {
  title: '',
  publisherId: '',
  publicationDate: '',
  isbn: '',
  locationId: '',
  languageId: '',
  genreId: '',
  pageCount: 1,
  authorIds: [],
};

const BookForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [genres, setGenres] = useState([]);
  const [languages, setLanguages] = useState([]);
  const [publishers, setPublishers] = useState([]);
  const [locations, setLocations] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    const load = async () => {
      setLoading(true);
      setError('');

      try {
        const requests = [
          getGenres(),
          getLanguages(),
          getPublishers(),
          getLocations(),
          getAuthors(),
        ];

        if (id) requests.push(getBook(id));

        const [genresResponse, languagesResponse, publishersResponse, locationsResponse, authorsResponse, bookResponse] =
          await Promise.all(requests);

        if (ignore) return;

        setGenres(genresResponse.data || []);
        setLanguages(languagesResponse.data || []);
        setPublishers(publishersResponse.data || []);
        setLocations(locationsResponse.data || []);
        setAuthors(authorsResponse.data || []);

        if (bookResponse) {
          const book = bookResponse.data;
          setForm({
            title: book.title || '',
            publisherId: book.publisherId || '',
            publicationDate: book.publicationDate || '',
            isbn: book.isbn || '',
            locationId: book.locationId || '',
            languageId: book.languageId || '',
            genreId: book.genreId || '',
            pageCount: book.pageCount || 1,
            authorIds: (book.authors || []).map((author) => author.id),
          });
        }
      } catch (loadError) {
        if (!ignore) {
          setError(getErrorMessage(loadError, 'Не удалось загрузить данные формы'));
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    load();

    return () => {
      ignore = true;
    };
  }, [id]);

  const update = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');

    try {
      const payload = {
        title: form.title.trim(),
        publisherId: form.publisherId || null,
        publicationDate: form.publicationDate,
        isbn: form.isbn.trim() || null,
        locationId: form.locationId || null,
        languageId: form.languageId,
        genreId: form.genreId,
        pageCount: Number(form.pageCount) || null,
        authorIds: form.authorIds,
      };

      const response = id ? await updateBook(id, payload) : await createBook(payload);
      toast.success(id ? 'Книга обновлена' : 'Книга создана');
      navigate(`/books/${response.data.id}`);
    } catch (saveError) {
      setError(getErrorMessage(saveError, 'Не удалось сохранить книгу'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="loading-state">Загрузка формы…</div>;
  }

  return (
    <div className="screen">
      <PageHeader
        title={id ? 'Редактирование книги' : 'Новая книга'}
        subtitle="Заполните сведения об издании"
        icon="book"
        breadcrumbs={
          <>
            <Link to="/catalog?type=books">Каталог</Link>
            <span>/</span>
            <span>{id ? 'Редактирование' : 'Новая книга'}</span>
          </>
        }
      />

      <div className="retro-panel panel form-box">
        {error ? <div className="form-error" role="alert">{error}</div> : null}

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group span-2">
              <label htmlFor="book-title">Название <span className="required">*</span></label>
              <input
                id="book-title"
                value={form.title}
                onChange={(event) => update('title', event.target.value)}
                maxLength={255}
                required
                autoFocus
              />
            </div>

            <div className="form-group">
              <label htmlFor="book-publisher">Издательство</label>
              <select
                id="book-publisher"
                value={form.publisherId}
                onChange={(event) => update('publisherId', event.target.value)}
              >
                <option value="">Не указано</option>
                {publishers.map((publisher) => (
                  <option key={publisher.id} value={publisher.id}>{publisher.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="book-date">Дата публикации <span className="required">*</span></label>
              <input
                id="book-date"
                type="date"
                value={form.publicationDate}
                onChange={(event) => update('publicationDate', event.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="book-genre">Жанр <span className="required">*</span></label>
              <select
                id="book-genre"
                value={form.genreId}
                onChange={(event) => update('genreId', event.target.value)}
                required
              >
                <option value="">Выберите жанр</option>
                {genres.map((genre) => (
                  <option key={genre.id} value={genre.id}>{genreLabel(genre.name)}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="book-language">Язык <span className="required">*</span></label>
              <select
                id="book-language"
                value={form.languageId}
                onChange={(event) => update('languageId', event.target.value)}
                required
              >
                <option value="">Выберите язык</option>
                {languages.map((language) => (
                  <option key={language.id} value={language.id}>{language.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="book-location">Расположение</label>
              <select
                id="book-location"
                value={form.locationId}
                onChange={(event) => update('locationId', event.target.value)}
              >
                <option value="">Не указано</option>
                {locations.map((location) => (
                  <option key={location.id} value={location.id}>{location.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="book-pages">Количество страниц</label>
              <input
                id="book-pages"
                type="number"
                min="1"
                value={form.pageCount}
                onChange={(event) => update('pageCount', event.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="book-isbn">ISBN</label>
              <input
                id="book-isbn"
                value={form.isbn}
                onChange={(event) => update('isbn', event.target.value)}
                maxLength={20}
                placeholder="978-5-…"
              />
            </div>

            <div className="form-group span-2">
              <label htmlFor="book-authors">
                Авторы
                <span className="field-hint">
                  {' '}— удерживайте Ctrl/Cmd для выбора нескольких ·{' '}
                  <Link to="/authors">управление авторами</Link>
                </span>
              </label>
              <select
                id="book-authors"
                multiple
                size={Math.min(Math.max(authors.length, 3), 7)}
                value={form.authorIds}
                onChange={(event) =>
                  update('authorIds', Array.from(event.target.selectedOptions, (option) => option.value))
                }
              >
                {authors.map((author) => (
                  <option key={author.id} value={author.id}>{author.fullName}</option>
                ))}
              </select>
              {authors.length === 0 ? (
                <span className="field-hint">
                  Авторов пока нет. <Link to="/authors">Добавьте автора</Link>.
                </span>
              ) : null}
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="retro-btn primary" disabled={saving}>
              <Icon name="check" size={13} />
              {saving ? 'Сохранение…' : 'Сохранить'}
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate('/catalog?type=books')}>
              Отмена
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BookForm;
