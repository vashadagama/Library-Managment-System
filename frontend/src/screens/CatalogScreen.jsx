import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { deleteBook, searchBooks } from '../api/books';
import { deleteMagazine, getMagazines } from '../api/magazines';
import { getGenres, getPublishers } from '../api/dictionaries';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { genreLabel } from '../utils/labels';
import useDebouncedValue from '../hooks/useDebouncedValue';
import ConfirmDialog from '../components/ConfirmDialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';
import Pagination from '../components/Pagination';

const CatalogScreen = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ROLE_ADMIN';

  const [type, setType] = useState(() =>
    searchParams.get('type') === 'magazines' ? 'magazines' : 'books'
  );
  const [search, setSearch] = useState(() => searchParams.get('search') || '');
  const [genreId, setGenreId] = useState(() => searchParams.get('genreId') || '');
  const [publisherId, setPublisherId] = useState(() => searchParams.get('publisherId') || '');
  const [yearFrom, setYearFrom] = useState(() => searchParams.get('yearFrom') || '');
  const [yearTo, setYearTo] = useState(() => searchParams.get('yearTo') || '');
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [genres, setGenres] = useState([]);
  const [publishers, setPublishers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const debouncedSearch = useDebouncedValue(search, 350);

  useEffect(() => {
    Promise.all([getGenres(), getPublishers()])
      .then(([genreResponse, publisherResponse]) => {
        setGenres(genreResponse.data || []);
        setPublishers(publisherResponse.data || []);
      })
      .catch(() => {
        setGenres([]);
        setPublishers([]);
      });
  }, []);

  useEffect(() => {
    const nextParams = new URLSearchParams();
    nextParams.set('type', type);
    if (debouncedSearch.trim()) nextParams.set('search', debouncedSearch.trim());
    if (genreId) nextParams.set('genreId', genreId);
    if (publisherId) nextParams.set('publisherId', publisherId);
    if (type === 'books' && yearFrom) nextParams.set('yearFrom', yearFrom);
    if (type === 'books' && yearTo) nextParams.set('yearTo', yearTo);

    setSearchParams(nextParams, { replace: true });
  }, [type, debouncedSearch, genreId, publisherId, yearFrom, yearTo, setSearchParams]);

  useEffect(() => {
    let ignore = false;

    const loadItems = async () => {
      setLoading(true);
      setError('');

      const term = debouncedSearch.trim();

      try {
        let response;

        if (type === 'books') {
          response = await searchBooks({
            page,
            size: 10,
            ...(term ? { title: term } : {}),
            ...(genreId ? { genreId } : {}),
            ...(publisherId ? { publisherId } : {}),
            ...(yearFrom ? { yearFrom: Number(yearFrom) } : {}),
            ...(yearTo ? { yearTo: Number(yearTo) } : {}),
          });
        } else {
          response = await getMagazines(page, 10, {
            ...(term ? { title: term } : {}),
            ...(genreId ? { genreId } : {}),
            ...(publisherId ? { publisherId } : {}),
          });
        }

        if (ignore) return;

        setItems(response.data?.content || []);
        setTotalPages(response.data?.totalPages || 0);
        setTotalElements(response.data?.totalElements || 0);
      } catch (loadError) {
        if (ignore) return;
        setItems([]);
        setTotalPages(0);
        setTotalElements(0);
        setError(getErrorMessage(loadError, 'Не удалось загрузить каталог'));
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    loadItems();

    return () => {
      ignore = true;
    };
  }, [type, page, debouncedSearch, genreId, publisherId, yearFrom, yearTo, reloadKey]);

  const resetFilters = () => {
    setSearch('');
    setGenreId('');
    setPublisherId('');
    setYearFrom('');
    setYearTo('');
    setPage(0);
  };

  const changeType = (nextType) => {
    if (nextType === type) return;
    setType(nextType);
    setSearch('');
    setGenreId('');
    setPublisherId('');
    setYearFrom('');
    setYearTo('');
    setPage(0);
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;

    setDeleting(true);
    try {
      if (deleteTarget.type === 'books') {
        await deleteBook(deleteTarget.id);
      } else {
        await deleteMagazine(deleteTarget.id);
      }
      toast.success('Издание удалено');
      setDeleteTarget(null);

      if (items.length === 1 && page > 0) {
        setPage((current) => current - 1);
      } else {
        setReloadKey((key) => key + 1);
      }
    } catch (deleteError) {
      toast.error(getErrorMessage(deleteError, 'Не удалось удалить издание'));
    } finally {
      setDeleting(false);
    }
  };

  const openItem = (item) => navigate(`/${type}/${item.id}`);

  const itemTypeLabel = type === 'books' ? 'книги' : 'журналы';
  const addPath = type === 'books' ? '/books/new' : '/magazines/new';
  const hasFilters = Boolean(search || genreId || publisherId || yearFrom || yearTo);
  const canDelete = isAdmin;

  return (
    <div className="screen">
      <PageHeader
        title="Каталог"
        subtitle={`Поиск и управление фондом · ${type === 'books' ? 'книги' : 'журналы'}`}
        icon="book"
        actions={
          <>
            <button type="button" className="retro-btn" onClick={() => setReloadKey((key) => key + 1)}>
              <Icon name="refresh" size={13} /> Обновить
            </button>
            <button type="button" className="retro-btn primary" onClick={() => navigate(addPath)}>
              <Icon name="plus" size={13} /> Добавить {type === 'books' ? 'книгу' : 'журнал'}
            </button>
          </>
        }
      />

      <div className="filter-bar">
        <div className="segmented">
          <button
            type="button"
            className={`retro-btn${type === 'books' ? ' active' : ''}`}
            onClick={() => changeType('books')}
          >
            <Icon name="book" size={13} /> Книги
          </button>
          <button
            type="button"
            className={`retro-btn${type === 'magazines' ? ' active' : ''}`}
            onClick={() => changeType('magazines')}
          >
            <Icon name="magazine" size={13} /> Журналы
          </button>
        </div>

        <div className="filter-field filter-field--grow">
          <span>Поиск по названию</span>
          <div className="search-field">
            <span className="search-icon"><Icon name="search" size={14} /></span>
            <input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(0);
              }}
              placeholder={`Название ${itemTypeLabel}…`}
            />
          </div>
        </div>

        <label className="filter-field">
          <span>Жанр</span>
          <select
            value={genreId}
            onChange={(event) => {
              setGenreId(event.target.value);
              setPage(0);
            }}
          >
            <option value="">Все жанры</option>
            {genres.map((genre) => (
              <option key={genre.id} value={genre.id}>{genreLabel(genre.name)}</option>
            ))}
          </select>
        </label>

        <label className="filter-field">
          <span>Издательство</span>
          <select
            value={publisherId}
            onChange={(event) => {
              setPublisherId(event.target.value);
              setPage(0);
            }}
          >
            <option value="">Все издательства</option>
            {publishers.map((publisher) => (
              <option key={publisher.id} value={publisher.id}>{publisher.name}</option>
            ))}
          </select>
        </label>

        {type === 'books' ? (
          <>
            <label className="filter-field">
              <span>Год от</span>
              <input
                type="number"
                min="0"
                max="2100"
                value={yearFrom}
                onChange={(event) => {
                  setYearFrom(event.target.value);
                  setPage(0);
                }}
                placeholder="1990"
              />
            </label>
            <label className="filter-field">
              <span>Год до</span>
              <input
                type="number"
                min="0"
                max="2100"
                value={yearTo}
                onChange={(event) => {
                  setYearTo(event.target.value);
                  setPage(0);
                }}
                placeholder="2026"
              />
            </label>
          </>
        ) : null}

        <div className="filter-actions">
          <button type="button" className="retro-btn" onClick={resetFilters} disabled={!hasFilters}>
            Сбросить
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-state">Загрузка каталога…</div>
      ) : error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={() => setReloadKey((key) => key + 1)}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : items.length === 0 ? (
        <div className="empty-state">
          <strong>{hasFilters ? 'Ничего не найдено' : 'Каталог пуст'}</strong>
          {hasFilters
            ? 'Измените запрос или сбросьте фильтры.'
            : `Добавьте первый ${type === 'books' ? 'книгу' : 'журнал'} в каталог.`}
        </div>
      ) : (
        <>
          <div className="table-wrap">
            <table className="retro-table">
              <thead>
                <tr>
                  <th>Название</th>
                  <th>{type === 'books' ? 'Авторы' : 'Издательство'}</th>
                  <th>Жанр</th>
                  <th>Год</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td className="title-cell">
                      <button
                        type="button"
                        className="link-button"
                        onClick={() => openItem(item)}
                      >
                        {item.title}
                      </button>
                      <div className="subline">
                        {item.publisherName || 'издательство не указано'}
                      </div>
                    </td>
                    <td>
                      {type === 'books'
                        ? item.authors?.map((author) => author.fullName || author.lastName).join(', ') || '—'
                        : item.publisherName || '—'}
                    </td>
                    <td>{genreLabel(item.genreName)}</td>
                    <td className="nowrap">{formatDate(item.publicationDate)}</td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="retro-btn retro-btn--small"
                          onClick={() => navigate(`/issue/${item.id}?type=${type}`)}
                        >
                          <Icon name="loans" size={12} /> Выдать
                        </button>
                        <button
                          type="button"
                          className="retro-btn retro-btn--small"
                          onClick={() => navigate(`/${type === 'books' ? 'books' : 'magazines'}/edit/${item.id}`)}
                        >
                          <Icon name="edit" size={12} /> Изменить
                        </button>
                        <button
                          type="button"
                          className="retro-btn retro-btn--small"
                          onClick={() => openItem(item)}
                        >
                          Открыть
                        </button>
                        {canDelete ? (
                          <button
                            type="button"
                            className="retro-btn retro-btn--small danger"
                            onClick={() => setDeleteTarget({ id: item.id, title: item.title, type })}
                            title="Удалить издание"
                          >
                            <Icon name="trash" size={12} />
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="catalog-footer">
            <span className="hint">
              Показано {items.length} из {totalElements}
            </span>
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              onChange={setPage}
            />
          </div>
        </>
      )}

      {deleteTarget ? (
        <ConfirmDialog
          title="Удаление издания"
          message={`Удалить «${deleteTarget.title}»? Если у издания есть экземпляры или история выдач, сервер может запретить удаление.`}
          confirmLabel="Удалить"
          danger
          busy={deleting}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      ) : null}
    </div>
  );
};

export default CatalogScreen;
