import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createBook, getBook, updateBook } from '../api/books';

const BookForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '',
    publisher: '',
    publicationDate: '',
    isbn: '',
    location: '',
    language: 'Русский',
    genre: 'FICTION',
    pageCount: 0
  });

  useEffect(() => {
    if (id) {
      getBook(id).then(res => {
        const b = res.data;
        setForm({
          title: b.title,
          publisher: b.publisher,
          publicationDate: b.publicationDate,
          isbn: b.isbn,
          location: b.location,
          language: b.language,
          genre: b.genre,
          pageCount: b.pageCount
        });
      });
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (id) {
        await updateBook(id, form);
      } else {
        await createBook(form);
      }
      navigate('/catalog?type=books');
    } catch (err) {
      alert('Ошибка сохранения: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>{id ? 'Редактировать книгу' : 'Новая книга'}</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Название</label><input required value={form.title} onChange={e => setForm({...form, title: e.target.value})} /></div>
        <div className="form-group"><label>Издатель</label><input value={form.publisher} onChange={e => setForm({...form, publisher: e.target.value})} /></div>
        <div className="form-group"><label>ISBN</label><input value={form.isbn} onChange={e => setForm({...form, isbn: e.target.value})} /></div>
        <div className="form-group"><label>Дата публикации</label><input type="date" value={form.publicationDate} onChange={e => setForm({...form, publicationDate: e.target.value})} /></div>
        <div className="form-group"><label>Расположение</label><input value={form.location} onChange={e => setForm({...form, location: e.target.value})} /></div>
        <div className="form-group"><label>Язык</label><input value={form.language} onChange={e => setForm({...form, language: e.target.value})} /></div>
        <div className="form-group"><label>Жанр</label>
          <select value={form.genre} onChange={e => setForm({...form, genre: e.target.value})}>
            <option value="FICTION">Художественная</option>
            <option value="NON_FICTION">Документальная</option>
            <option value="SCIENCE">Наука</option>
            <option value="HISTORY">История</option>
            <option value="BIOGRAPHY">Биография</option>
            <option value="CHILDREN">Детская</option>
            <option value="TECHNICAL">Техническая</option>
            <option value="ART">Искусство</option>
          </select>
        </div>
        <div className="form-group"><label>Страниц</label><input type="number" value={form.pageCount} onChange={e => setForm({...form, pageCount: parseInt(e.target.value)})} /></div>
        <button type="submit" disabled={loading}>{loading ? 'Сохранение...' : 'Сохранить'}</button>
        <button type="button" onClick={() => navigate(-1)}>Отмена</button>
      </form>
    </div>
  );
};

export default BookForm;