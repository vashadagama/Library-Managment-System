import { useCallback, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { createAuthor, deleteAuthor, getAuthors, updateAuthor } from '../api/authors';
import { getErrorMessage } from '../utils/errors';
import ConfirmDialog from '../components/ConfirmDialog';
import Dialog from '../components/Dialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const emptyAuthor = {
  id: null,
  firstName: '',
  lastName: '',
  middleName: '',
};

const AuthorsScreen = () => {
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [editor, setEditor] = useState(null);
  const [form, setForm] = useState(emptyAuthor);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadAuthors = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await getAuthors();
      setAuthors(Array.isArray(response.data) ? response.data : []);
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить авторов'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAuthors();
  }, [loadAuthors]);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return authors;

    return authors.filter((author) =>
      [author.fullName, author.firstName, author.lastName, author.middleName]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term))
    );
  }, [authors, search]);

  const openCreate = () => {
    setEditor('create');
    setForm(emptyAuthor);
    setFormError('');
  };

  const openEdit = (author) => {
    setEditor('edit');
    setForm({
      id: author.id,
      firstName: author.firstName || '',
      lastName: author.lastName || '',
      middleName: author.middleName || '',
    });
    setFormError('');
  };

  const closeEditor = () => {
    if (saving) return;
    setEditor(null);
    setForm(emptyAuthor);
    setFormError('');
  };

  const update = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const saveAuthor = async () => {
    if (!form.firstName.trim() || !form.lastName.trim()) {
      setFormError('Имя и фамилия обязательны');
      return;
    }

    setSaving(true);
    setFormError('');

    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        middleName: form.middleName.trim() || null,
      };

      if (form.id) {
        await updateAuthor(form.id, payload);
        toast.success('Автор обновлён');
      } else {
        await createAuthor(payload);
        toast.success('Автор добавлен');
      }

      setEditor(null);
      setForm(emptyAuthor);
      await loadAuthors();
    } catch (saveError) {
      setFormError(getErrorMessage(saveError, 'Не удалось сохранить автора'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;

    setDeleting(true);
    try {
      await deleteAuthor(deleteTarget.id);
      toast.success('Автор удалён');
      setDeleteTarget(null);
      await loadAuthors();
    } catch (deleteError) {
      toast.error(getErrorMessage(deleteError, 'Не удалось удалить автора'));
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="screen">
      <PageHeader
        title="Авторы"
        subtitle="Справочник авторов книг"
        icon="authors"
        actions={
          <>
            <button type="button" className="retro-btn" onClick={loadAuthors}>
              <Icon name="refresh" size={13} /> Обновить
            </button>
            <button type="button" className="retro-btn primary" onClick={openCreate}>
              <Icon name="plus" size={13} /> Добавить автора
            </button>
          </>
        }
      />

      <div className="filter-bar">
        <div className="filter-field filter-field--grow">
          <span>Поиск автора</span>
          <div className="search-field">
            <span className="search-icon"><Icon name="search" size={14} /></span>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Фамилия, имя или отчество…"
            />
          </div>
        </div>
        <div className="filter-actions">
          <button type="button" className="retro-btn" onClick={() => setSearch('')} disabled={!search}>
            Сбросить
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-state">Загрузка авторов…</div>
      ) : error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={loadAuthors}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <strong>{search ? 'Авторы не найдены' : 'Список авторов пуст'}</strong>
          {search ? 'Измените поисковый запрос.' : 'Добавьте первого автора.'}
        </div>
      ) : (
        <div className="table-wrap">
          <table className="retro-table">
            <thead>
              <tr>
                <th>ФИО</th>
                <th>Имя</th>
                <th>Фамилия</th>
                <th>Отчество</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((author) => (
                <tr key={author.id}>
                  <td className="title-cell">{author.fullName}</td>
                  <td>{author.firstName}</td>
                  <td>{author.lastName}</td>
                  <td>{author.middleName || '—'}</td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="retro-btn retro-btn--small" onClick={() => openEdit(author)}>
                        <Icon name="edit" size={12} /> Изменить
                      </button>
                      <button
                        type="button"
                        className="retro-btn retro-btn--small danger"
                        onClick={() => setDeleteTarget(author)}
                      >
                        <Icon name="trash" size={12} /> Удалить
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editor ? (
        <Dialog
          title={form.id ? 'Редактирование автора' : 'Новый автор'}
          icon="authors"
          onClose={closeEditor}
          footer={
            <>
              <button type="button" className="retro-btn" onClick={closeEditor} disabled={saving}>
                Отмена
              </button>
              <button type="button" className="retro-btn primary" onClick={saveAuthor} disabled={saving}>
                {saving ? 'Сохранение…' : 'Сохранить'}
              </button>
            </>
          }
        >
          {formError ? <div className="form-error">{formError}</div> : null}
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="author-first">Имя <span className="required">*</span></label>
              <input
                id="author-first"
                value={form.firstName}
                onChange={(event) => update('firstName', event.target.value)}
                required
                autoFocus
              />
            </div>
            <div className="form-group">
              <label htmlFor="author-last">Фамилия <span className="required">*</span></label>
              <input
                id="author-last"
                value={form.lastName}
                onChange={(event) => update('lastName', event.target.value)}
                required
              />
            </div>
            <div className="form-group span-2">
              <label htmlFor="author-middle">Отчество</label>
              <input
                id="author-middle"
                value={form.middleName}
                onChange={(event) => update('middleName', event.target.value)}
              />
            </div>
          </div>
        </Dialog>
      ) : null}

      {deleteTarget ? (
        <ConfirmDialog
          title="Удаление автора"
          message={`Удалить автора ${deleteTarget.fullName}? Если у него есть книги, сервер запретит удаление.`}
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

export default AuthorsScreen;
