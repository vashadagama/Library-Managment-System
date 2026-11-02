import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getMagazine } from '../api/magazines';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { genreLabel } from '../utils/labels';
import CopiesPanel from '../components/CopiesPanel';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const MagazineDetailScreen = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [magazine, setMagazine] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const canManage = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_LIBRARIAN';
  const canDelete = user?.role === 'ROLE_ADMIN';

  const loadMagazine = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await getMagazine(id);
      setMagazine(response.data);
    } catch (loadError) {
      setMagazine(null);
      setError(getErrorMessage(loadError, 'Не удалось загрузить журнал'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadMagazine();
  }, [loadMagazine, reloadKey]);

  if (loading) {
    return <div className="loading-state">Загрузка журнала…</div>;
  }

  if (error || !magazine) {
    return (
      <div className="screen">
        <PageHeader
          title="Журнал"
          icon="magazine"
          breadcrumbs={<Link to="/catalog?type=magazines">Каталог</Link>}
        />
        <div className="error-state">
          <strong>Журнал не загружен</strong>
          {error || 'Журнал не найден'}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={loadMagazine}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
            <button type="button" className="retro-btn retro-btn--small" onClick={() => navigate('/catalog?type=magazines')}>
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
        title={magazine.title}
        subtitle={magazine.publisherName || 'Издательство не указано'}
        icon="magazine"
        breadcrumbs={
          <>
            <Link to="/catalog?type=magazines">Каталог</Link>
            <span>/</span>
            <span>{magazine.title}</span>
          </>
        }
        actions={
          <>
            <button type="button" className="retro-btn" onClick={() => navigate('/catalog?type=magazines')}>
              <Icon name="arrowLeft" size={13} /> Назад
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate(`/issue/${magazine.id}?type=magazines`)}>
              <Icon name="loans" size={13} /> Выдать
            </button>
            {canManage ? (
              <button type="button" className="retro-btn primary" onClick={() => navigate(`/magazines/edit/${magazine.id}`)}>
                <Icon name="edit" size={13} /> Изменить
              </button>
            ) : null}
          </>
        }
      />

      <section className="retro-panel panel">
        <div className="section-title">
          <span><Icon name="info" size={14} /> Сведения о журнале</span>
        </div>
        <div className="meta-grid">
          <div className="meta-row">
            <span className="meta-row__label">Издательство</span>
            <span className="meta-row__value">{magazine.publisherName || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Жанр</span>
            <span className="meta-row__value">{genreLabel(magazine.genreName)}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Язык</span>
            <span className="meta-row__value">{magazine.languageName || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Дата выпуска</span>
            <span className="meta-row__value">{formatDate(magazine.publicationDate)}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">ISSN</span>
            <span className="meta-row__value">{magazine.issn || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Страниц</span>
            <span className="meta-row__value">{magazine.pageCount ?? '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Расположение</span>
            <span className="meta-row__value">{magazine.locationName || '—'}</span>
          </div>
          <div className="meta-row">
            <span className="meta-row__label">Обложка</span>
            <span className="meta-row__value">{magazine.hasGlossyCover ? 'Глянцевая' : 'Обычная'}</span>
          </div>
        </div>
      </section>

      <CopiesPanel itemId={magazine.id} canManage={canManage} canDelete={canDelete} />
    </div>
  );
};

export default MagazineDetailScreen;
