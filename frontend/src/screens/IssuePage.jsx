import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getBook } from '../api/books';
import { getCopies } from '../api/copies';
import { issueByLibraryItem } from '../api/loans';
import { getMagazine } from '../api/magazines';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { genreLabel } from '../utils/labels';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';
import ReaderPicker from '../components/ReaderPicker';

const IssuePage = () => {
  const { itemId } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') === 'magazines' ? 'magazines' : 'books';
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [copies, setCopies] = useState([]);
  const [reader, setReader] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [issuing, setIssuing] = useState(false);

  useEffect(() => {
    let ignore = false;

    const load = async () => {
      setLoading(true);
      setError('');

      try {
        const itemRequest = type === 'books' ? getBook(itemId) : getMagazine(itemId);
        const [itemResponse, copiesResponse] = await Promise.all([itemRequest, getCopies(itemId)]);

        if (ignore) return;
        setItem(itemResponse.data);
        setCopies(Array.isArray(copiesResponse.data) ? copiesResponse.data : []);
      } catch (loadError) {
        if (!ignore) {
          setError(getErrorMessage(loadError, 'Не удалось загрузить издание'));
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    load();

    return () => {
      ignore = true;
    };
  }, [itemId, type]);

  const availableCopies = useMemo(
    () => copies.filter((copy) => copy.statusName === 'AVAILABLE'),
    [copies]
  );

  const handleIssue = async () => {
    if (!reader) {
      toast.warning('Выберите читателя');
      return;
    }

    setIssuing(true);
    try {
      const response = await issueByLibraryItem(reader.id, itemId);
      toast.success(`Издание выдано: ${response.data?.userFullName || reader.fullName}`);
      navigate('/loans');
    } catch (issueError) {
      toast.error(getErrorMessage(issueError, 'Не удалось выдать издание'));
    } finally {
      setIssuing(false);
    }
  };

  if (loading) {
    return <div className="loading-state">Загрузка издания…</div>;
  }

  if (error || !item) {
    return (
      <div className="screen">
        <PageHeader
          title="Выдача издания"
          icon="loans"
          breadcrumbs={<Link to="/catalog">Каталог</Link>}
        />
        <div className="error-state">
          <strong>Издание не загружено</strong>
          {error || 'Издание не найдено'}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={() => navigate('/catalog')}>
              <Icon name="arrowLeft" size={13} /> В каталог
            </button>
          </div>
        </div>
      </div>
    );
  }

  const detailPath = type === 'books' ? `/books/${item.id}` : `/magazines/${item.id}`;
  const catalogPath = type === 'books' ? '/catalog?type=books' : '/catalog?type=magazines';
  const authors = item.authors?.map((author) => author.fullName).join(', ');
  const description = type === 'books'
    ? authors || 'Авторы не указаны'
    : item.publisherName || 'Издательство не указано';

  return (
    <div className="screen">
      <PageHeader
        title="Выдача издания"
        subtitle={item.title}
        icon="loans"
        breadcrumbs={
          <>
            <Link to={catalogPath}>Каталог</Link>
            <span>/</span>
            <Link to={detailPath}>{item.title}</Link>
            <span>/</span>
            <span>Выдача</span>
          </>
        }
        actions={
          <button type="button" className="retro-btn" onClick={() => navigate(detailPath)}>
            <Icon name="arrowLeft" size={13} /> К изданию
          </button>
        }
      />

      <div className="issue-layout">
        <section className="retro-panel panel">
          <div className="section-title">
            <span><Icon name={type === 'books' ? 'book' : 'magazine'} size={14} /> Издание</span>
          </div>
          <div className="issue-title">{item.title}</div>
          <div className="issue-subtitle">{description}</div>
          <div className="meta-grid meta-grid--single">
            <div className="meta-row">
              <span className="meta-row__label">Тип</span>
              <span className="meta-row__value">{type === 'books' ? 'Книга' : 'Журнал'}</span>
            </div>
            <div className="meta-row">
              <span className="meta-row__label">Жанр</span>
              <span className="meta-row__value">{genreLabel(item.genreName)}</span>
            </div>
            <div className="meta-row">
              <span className="meta-row__label">Дата публикации</span>
              <span className="meta-row__value">{formatDate(item.publicationDate)}</span>
            </div>
            <div className="meta-row">
              <span className="meta-row__label">Доступно экземпляров</span>
              <span className="meta-row__value">
                <span className={`badge ${availableCopies.length > 0 ? 'success' : 'danger'}`}>
                  {availableCopies.length} из {copies.length}
                </span>
              </span>
            </div>
          </div>
        </section>

        <section className="retro-panel panel">
          <div className="section-title">
            <span><Icon name="readers" size={14} /> Читатель</span>
          </div>
          <div className="form-group">
            <label>Кому выдать издание</label>
            <ReaderPicker value={reader} onChange={setReader} />
          </div>

          {reader ? (
            <div className="issue-reader-info">
              <div><strong>{reader.fullName}</strong></div>
              <div className="hint">
                {reader.libraryCardNumber || 'без номера билета'} · на руках: {reader.activeLoansCount ?? 0}
                {reader.maxActiveLoans != null ? ` из ${reader.maxActiveLoans}` : ''}
              </div>
            </div>
          ) : (
            <p className="hint">Начните вводить фамилию, email или номер билета читателя.</p>
          )}

          {availableCopies.length === 0 ? (
            <div className="form-error">
              Нет доступных экземпляров. Добавьте экземпляр или измените его статус на странице издания.
            </div>
          ) : null}

          <div className="form-actions">
            <button
              type="button"
              className="retro-btn primary"
              onClick={handleIssue}
              disabled={issuing || !reader || availableCopies.length === 0}
            >
              <Icon name="check" size={13} />
              {issuing ? 'Выдача…' : 'Выдать издание'}
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate(detailPath)}>
              Отмена
            </button>
          </div>
        </section>
      </div>
    </div>
  );
};

export default IssuePage;
