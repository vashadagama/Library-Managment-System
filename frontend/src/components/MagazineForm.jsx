import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createMagazine, getMagazine, updateMagazine } from '../api/magazines';

const MagazineForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '',
    publisher: '',
    publicationDate: '',
    issn: '',
    location: 'Стенд журналов',
    language: 'Русский',
    genre: 'POPULAR_SCIENTIFIC',
    pageCount: 0,
    hasGlossyCover: false
  });

  useEffect(() => {
    if (id) {
      getMagazine(id).then(res => {
        const m = res.data;
        setForm({
          title: m.title,
          publisher: m.publisher,
          publicationDate: m.publicationDate,
          issn: m.issn,
          location: m.location,
          language: m.language,
          genre: m.genre,
          pageCount: m.pageCount,
          hasGlossyCover: m.hasGlossyCover
        });
      });
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (id) {
        await updateMagazine(id, form);
      } else {
        await createMagazine(form);
      }
      navigate('/catalog?type=magazines');
    } catch (err) {
      alert('Ошибка: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>{id ? 'Редактировать журнал' : 'Новый журнал'}</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Название</label><input required value={form.title} onChange={e => setForm({...form, title: e.target.value})} /></div>
        <div className="form-group"><label>Издатель</label><input value={form.publisher} onChange={e => setForm({...form, publisher: e.target.value})} /></div>
        <div className="form-group"><label>ISSN</label><input value={form.issn} onChange={e => setForm({...form, issn: e.target.value})} /></div>
        <div className="form-group"><label>Дата публикации</label><input type="date" value={form.publicationDate} onChange={e => setForm({...form, publicationDate: e.target.value})} /></div>
        <div className="form-group"><label>Расположение</label><input value={form.location} onChange={e => setForm({...form, location: e.target.value})} /></div>
        <div className="form-group"><label>Язык</label><input value={form.language} onChange={e => setForm({...form, language: e.target.value})} /></div>
        <div className="form-group"><label>Жанр</label>
          <select value={form.genre} onChange={e => setForm({...form, genre: e.target.value})}>
            <option value="CHILDRENS">Детский</option>
            <option value="POPULAR_SCIENTIFIC">Научно-популярный</option>
            <option value="MALE">Мужской</option>
            <option value="FEMALE">Женский</option>
            <option value="SCIENTIFIC">Научный</option>
          </select>
        </div>
        <div className="form-group"><label>Страниц</label><input type="number" value={form.pageCount} onChange={e => setForm({...form, pageCount: parseInt(e.target.value)})} /></div>
        <div className="form-group"><label><input type="checkbox" checked={form.hasGlossyCover} onChange={e => setForm({...form, hasGlossyCover: e.target.checked})} /> Глянцевая обложка</label></div>
        <button type="submit" disabled={loading}>{loading ? 'Сохранение...' : 'Сохранить'}</button>
        <button type="button" onClick={() => navigate(-1)}>Отмена</button>
      </form>
    </div>
  );
};

export default MagazineForm;