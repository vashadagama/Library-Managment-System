import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { createMagazine, getMagazine, updateMagazine } from '../api/magazines';
import { getGenres, getLanguages, getLocations, getPublishers } from '../api/dictionaries';
import { getErrorMessage } from '../utils/errors';
import { genreLabel } from '../utils/labels';
import Icon from './Icon';
import PageHeader from './PageHeader';

const emptyForm = {
  title: '',
  publisherId: '',
  publicationDate: '',
  issn: '',
  locationId: '',
  languageId: '',
  genreId: '',
  pageCount: 1,
  hasGlossyCover: false,
};

const MagazineForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [genres, setGenres] = useState([]);
  const [languages, setLanguages] = useState([]);
  const [publishers, setPublishers] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    const load = async () => {
      setLoading(true);
      setError('');

      try {
        const requests = [getGenres(), getLanguages(), getPublishers(), getLocations()];
        if (id) requests.push(getMagazine(id));

        const [genresResponse, languagesResponse, publishersResponse, locationsResponse, magazineResponse] =
          await Promise.all(requests);

        if (ignore) return;

        setGenres(genresResponse.data || []);
        setLanguages(languagesResponse.data || []);
        setPublishers(publishersResponse.data || []);
        setLocations(locationsResponse.data || []);

        if (magazineResponse) {
          const magazine = magazineResponse.data;
          setForm({
            title: magazine.title || '',
            publisherId: magazine.publisherId || '',
            publicationDate: magazine.publicationDate || '',
            issn: magazine.issn || '',
            locationId: magazine.locationId || '',
            languageId: magazine.languageId || '',
            genreId: magazine.genreId || '',
            pageCount: magazine.pageCount || 1,
            hasGlossyCover: Boolean(magazine.hasGlossyCover),
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
        issn: form.issn.trim() || null,
        locationId: form.locationId || null,
        languageId: form.languageId,
        genreId: form.genreId,
        pageCount: Number(form.pageCount) || null,
        hasGlossyCover: Boolean(form.hasGlossyCover),
      };

      const response = id ? await updateMagazine(id, payload) : await createMagazine(payload);
      toast.success(id ? 'Журнал обновлён' : 'Журнал создан');
      navigate(`/magazines/${response.data.id}`);
    } catch (saveError) {
      setError(getErrorMessage(saveError, 'Не удалось сохранить журнал'));
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
        title={id ? 'Редактирование журнала' : 'Новый журнал'}
        subtitle="Заполните сведения о выпуске"
        icon="magazine"
        breadcrumbs={
          <>
            <Link to="/catalog?type=magazines">Каталог</Link>
            <span>/</span>
            <span>{id ? 'Редактирование' : 'Новый журнал'}</span>
          </>
        }
      />

      <div className="retro-panel panel form-box">
        {error ? <div className="form-error" role="alert">{error}</div> : null}

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group span-2">
              <label htmlFor="magazine-title">Название <span className="required">*</span></label>
              <input
                id="magazine-title"
                value={form.title}
                onChange={(event) => update('title', event.target.value)}
                maxLength={255}
                required
                autoFocus
              />
            </div>

            <div className="form-group">
              <label htmlFor="magazine-publisher">Издательство</label>
              <select
                id="magazine-publisher"
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
              <label htmlFor="magazine-date">Дата выпуска <span className="required">*</span></label>
              <input
                id="magazine-date"
                type="date"
                value={form.publicationDate}
                onChange={(event) => update('publicationDate', event.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="magazine-genre">Жанр <span className="required">*</span></label>
              <select
                id="magazine-genre"
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
              <label htmlFor="magazine-language">Язык <span className="required">*</span></label>
              <select
                id="magazine-language"
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
              <label htmlFor="magazine-location">Расположение</label>
              <select
                id="magazine-location"
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
              <label htmlFor="magazine-pages">Количество страниц</label>
              <input
                id="magazine-pages"
                type="number"
                min="1"
                value={form.pageCount}
                onChange={(event) => update('pageCount', event.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="magazine-issn">ISSN</label>
              <input
                id="magazine-issn"
                value={form.issn}
                onChange={(event) => update('issn', event.target.value)}
                maxLength={20}
                placeholder="1234-5678"
              />
            </div>

            <div className="form-group span-2">
              <label className="checkbox-label" htmlFor="magazine-glossy">
                <input
                  id="magazine-glossy"
                  type="checkbox"
                  checked={form.hasGlossyCover}
                  onChange={(event) => update('hasGlossyCover', event.target.checked)}
                />
                Глянцевая обложка
              </label>
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="retro-btn primary" disabled={saving}>
              <Icon name="check" size={13} />
              {saving ? 'Сохранение…' : 'Сохранить'}
            </button>
            <button type="button" className="retro-btn" onClick={() => navigate('/catalog?type=magazines')}>
              Отмена
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default MagazineForm;
