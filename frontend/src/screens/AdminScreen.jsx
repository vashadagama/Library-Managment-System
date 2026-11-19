import { useCallback, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import {
  createDictionaryEntry,
  deleteDictionaryEntry,
  getAuditLog,
  getDictionaryEntries,
  getSystemParameters,
  updateDictionaryEntry,
  updateSystemParameter,
} from '../api/admin';
import { getItemStatuses, getLoanStatuses, getRoles, getUserStatuses } from '../api/dictionaries';
import ConfirmDialog from '../components/ConfirmDialog';
import Dialog from '../components/Dialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/errors';
import { formatDateTime } from '../utils/format';

const TABS = [
  { key: 'params', label: 'Системные параметры', icon: 'settings' },
  { key: 'dicts', label: 'Справочники', icon: 'box' },
  { key: 'audit', label: 'Журнал аудита', icon: 'clock' },
];

const EDITABLE_DICTS = [
  {
    key: 'genres',
    label: 'Жанры',
    fields: [{ name: 'name', label: 'Название', required: true }],
  },
  {
    key: 'languages',
    label: 'Языки',
    fields: [{ name: 'name', label: 'Название', required: true }],
  },
  {
    key: 'publishers',
    label: 'Издательства',
    fields: [
      { name: 'name', label: 'Название', required: true },
      { name: 'city', label: 'Город' },
    ],
  },
  {
    key: 'locations',
    label: 'Места хранения',
    fields: [
      { name: 'hall', label: 'Зал' },
      { name: 'shelf', label: 'Стеллаж' },
      { name: 'rack', label: 'Полка' },
    ],
  },
];

const READONLY_DICTS = [
  { key: 'roles', label: 'Роли пользователей', loader: getRoles },
  { key: 'user-statuses', label: 'Статусы пользователей', loader: getUserStatuses },
  { key: 'item-statuses', label: 'Статусы экземпляров', loader: getItemStatuses },
  { key: 'loan-statuses', label: 'Статусы выдач', loader: getLoanStatuses },
];

const AUDIT_PAGE_SIZE = 20;

const emptyForm = (fields) => {
  const form = {};
  fields.forEach((field) => {
    form[field.name] = '';
  });
  return form;
};

const entryTitle = (entry) => entry.name || entry.hall || entry.id;

const AdminScreen = () => {
  const [tab, setTab] = useState('params');

  const [params, setParams] = useState([]);
  const [paramDraft, setParamDraft] = useState({});
  const [paramsLoading, setParamsLoading] = useState(false);
  const [paramsError, setParamsError] = useState('');
  const [paramSaving, setParamSaving] = useState('');

  const [dictType, setDictType] = useState('genres');
  const [entries, setEntries] = useState([]);
  const [dictLoading, setDictLoading] = useState(false);
  const [dictError, setDictError] = useState('');
  const [createForm, setCreateForm] = useState(() => emptyForm(EDITABLE_DICTS[0].fields));
  const [creating, setCreating] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [editForm, setEditForm] = useState({});
  const [savingEntry, setSavingEntry] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const [auditPage, setAuditPage] = useState(0);
  const [audit, setAudit] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [auditLoading, setAuditLoading] = useState(false);
  const [auditError, setAuditError] = useState('');

  const dictConfig = useMemo(
    () => EDITABLE_DICTS.find((item) => item.key === dictType) || null,
    [dictType]
  );
  const readonlyConfig = useMemo(
    () => READONLY_DICTS.find((item) => item.key === dictType) || null,
    [dictType]
  );

  const loadParams = useCallback(async () => {
    setParamsLoading(true);
    setParamsError('');
    try {
      const response = await getSystemParameters();
      const list = Array.isArray(response.data) ? response.data : [];
      setParams(list);
      const draft = {};
      list.forEach((item) => {
        draft[item.key] = item.value ?? '';
      });
      setParamDraft(draft);
    } catch (error) {
      setParamsError(getErrorMessage(error, 'Не удалось загрузить системные параметры'));
    } finally {
      setParamsLoading(false);
    }
  }, []);

  const loadEntries = useCallback(async (type) => {
    setDictLoading(true);
    setDictError('');
    try {
      const editable = EDITABLE_DICTS.find((item) => item.key === type);
      if (editable) {
        const response = await getDictionaryEntries(type);
        setEntries(Array.isArray(response.data) ? response.data : []);
      } else {
        const readonly = READONLY_DICTS.find((item) => item.key === type);
        const response = await readonly.loader();
        setEntries(Array.isArray(response.data) ? response.data : []);
      }
    } catch (error) {
      setEntries([]);
      setDictError(getErrorMessage(error, 'Не удалось загрузить справочник'));
    } finally {
      setDictLoading(false);
    }
  }, []);

  const loadAudit = useCallback(async (page) => {
    setAuditLoading(true);
    setAuditError('');
    try {
      const response = await getAuditLog(page, AUDIT_PAGE_SIZE);
      setAudit({
        content: response.data?.content || [],
        totalPages: response.data?.totalPages || 0,
        totalElements: response.data?.totalElements || 0,
      });
    } catch (error) {
      setAudit({ content: [], totalPages: 0, totalElements: 0 });
      setAuditError(getErrorMessage(error, 'Не удалось загрузить журнал аудита'));
    } finally {
      setAuditLoading(false);
    }
  }, []);

  useEffect(() => {
    loadParams();
  }, [loadParams]);

  useEffect(() => {
    if (tab !== 'dicts') return;
    const config = EDITABLE_DICTS.find((item) => item.key === dictType);
    setCreateForm(emptyForm(config ? config.fields : []));
    loadEntries(dictType);
  }, [tab, dictType, loadEntries]);

  useEffect(() => {
    if (tab !== 'audit') return;
    loadAudit(auditPage);
  }, [tab, auditPage, loadAudit]);

  const saveParam = async (parameter) => {
    const value = paramDraft[parameter.key];
    if (value == null || String(value).trim() === '') {
      toast.error('Значение параметра не может быть пустым');
      return;
    }

    setParamSaving(parameter.key);
    try {
      await updateSystemParameter(parameter.key, String(value).trim());
      toast.success('Параметр сохранён');
      loadParams();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Не удалось сохранить параметр'));
    } finally {
      setParamSaving('');
    }
  };

  const submitCreate = async () => {
    if (!dictConfig) return;

    const payload = {};
    for (const field of dictConfig.fields) {
      const value = String(createForm[field.name] ?? '').trim();
      if (field.required && !value) {
        toast.error(`Заполните поле «${field.label}»`);
        return;
      }
      payload[field.name] = value;
    }

    setCreating(true);
    try {
      await createDictionaryEntry(dictConfig.key, payload);
      toast.success('Запись добавлена');
      setCreateForm(emptyForm(dictConfig.fields));
      loadEntries(dictConfig.key);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Не удалось добавить запись'));
    } finally {
      setCreating(false);
    }
  };

  const openEdit = (entry) => {
    if (!dictConfig) return;
    const form = {};
    dictConfig.fields.forEach((field) => {
      form[field.name] = entry[field.name] ?? '';
    });
    setEditForm(form);
    setEditTarget(entry);
  };

  const submitEdit = async () => {
    if (!dictConfig || !editTarget) return;

    const payload = {};
    for (const field of dictConfig.fields) {
      const value = String(editForm[field.name] ?? '').trim();
      if (field.required && !value) {
        toast.error(`Заполните поле «${field.label}»`);
        return;
      }
      payload[field.name] = value;
    }

    setSavingEntry(true);
    try {
      await updateDictionaryEntry(dictConfig.key, editTarget.id, payload);
      toast.success('Запись обновлена');
      setEditTarget(null);
      loadEntries(dictConfig.key);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Не удалось обновить запись'));
    } finally {
      setSavingEntry(false);
    }
  };

  const confirmDelete = async () => {
    if (!dictConfig || !deleteTarget) return;

    setDeleting(true);
    try {
      await deleteDictionaryEntry(dictConfig.key, deleteTarget.id);
      toast.success('Запись удалена');
      setDeleteTarget(null);
      loadEntries(dictConfig.key);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Не удалось удалить запись'));
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="screen">
      <PageHeader
        title="Администрирование"
        subtitle="Справочники-классификаторы, системные параметры и журнал аудита"
        icon="settings"
      />

      <div className="segmented admin-tabs">
        {TABS.map((item) => (
          <button
            key={item.key}
            type="button"
            className={`retro-btn${tab === item.key ? ' active' : ''}`}
            onClick={() => setTab(item.key)}
          >
            <Icon name={item.icon} size={13} /> {item.label}
          </button>
        ))}
      </div>

      {tab === 'params' ? (
        <section className="retro-panel panel">
          <div className="section-title">
            <span>
              <Icon name="settings" size={14} /> Системные параметры
            </span>
            <span className="section-title__meta">Изменения применяются без перезапуска</span>
          </div>

          {paramsError ? <div className="form-error">{paramsError}</div> : null}
          {paramsLoading ? <div className="loading-state">Загрузка параметров…</div> : null}

          {!paramsLoading && !paramsError ? (
            <div className="table-wrap">
              <table className="retro-table">
                <thead>
                  <tr>
                    <th>Параметр</th>
                    <th>Описание</th>
                    <th>Значение</th>
                    <th>Действия</th>
                  </tr>
                </thead>
                <tbody>
                  {params.map((parameter) => (
                    <tr key={parameter.key}>
                      <td className="nowrap"><code>{parameter.key}</code></td>
                      <td>{parameter.description || '—'}</td>
                      <td>
                        <input
                          className="retro-input admin-param-input"
                          value={paramDraft[parameter.key] ?? ''}
                          onChange={(event) =>
                            setParamDraft((current) => ({
                              ...current,
                              [parameter.key]: event.target.value,
                            }))
                          }
                        />
                      </td>
                      <td>
                        <button
                          type="button"
                          className="retro-btn retro-btn--small primary"
                          onClick={() => saveParam(parameter)}
                          disabled={paramSaving === parameter.key}
                        >
                          <Icon name="check" size={12} />{' '}
                          {paramSaving === parameter.key ? 'Сохранение…' : 'Сохранить'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>
      ) : null}

      {tab === 'dicts' ? (
        <section className="retro-panel panel">
          <div className="section-title">
            <span>
              <Icon name="box" size={14} /> Справочники
            </span>
            <span className="section-title__meta">
              {dictConfig ? 'Доступно редактирование' : 'Системный справочник — только просмотр'}
            </span>
          </div>

          <div className="filter-bar">
            <div className="filter-field">
              <span>Справочник</span>
              <select
                className="retro-input"
                value={dictType}
                onChange={(event) => setDictType(event.target.value)}
              >
                <optgroup label="Редактируемые">
                  {EDITABLE_DICTS.map((item) => (
                    <option key={item.key} value={item.key}>{item.label}</option>
                  ))}
                </optgroup>
                <optgroup label="Системные">
                  {READONLY_DICTS.map((item) => (
                    <option key={item.key} value={item.key}>{item.label}</option>
                  ))}
                </optgroup>
              </select>
            </div>
          </div>

          {dictConfig ? (
            <div className="admin-dict-form">
              {dictConfig.fields.map((field) => (
                <div className="form-group" key={field.name}>
                  <label htmlFor={`dict-create-${field.name}`}>
                    {field.label} {field.required ? <span className="required">*</span> : null}
                  </label>
                  <input
                    id={`dict-create-${field.name}`}
                    className="retro-input"
                    value={createForm[field.name] ?? ''}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, [field.name]: event.target.value }))
                    }
                  />
                </div>
              ))}
              <button
                type="button"
                className="retro-btn primary admin-dict-form__submit"
                onClick={submitCreate}
                disabled={creating}
              >
                <Icon name="plus" size={13} /> {creating ? 'Добавление…' : 'Добавить'}
              </button>
            </div>
          ) : null}

          {dictError ? <div className="form-error">{dictError}</div> : null}
          {dictLoading ? <div className="loading-state">Загрузка справочника…</div> : null}

          {!dictLoading && !dictError ? (
            entries.length === 0 ? (
              <div className="empty-state">
                <strong>Записей нет</strong>
                {dictConfig ? 'Добавьте первую запись.' : 'Справочник пуст.'}
              </div>
            ) : (
              <div className="table-wrap">
                <table className="retro-table">
                  <thead>
                    <tr>
                      {dictConfig
                        ? dictConfig.fields.map((field) => <th key={field.name}>{field.label}</th>)
                        : <th>Наименование</th>}
                      {dictConfig ? <th>Действия</th> : null}
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((entry) => (
                      <tr key={entry.id}>
                        {dictConfig
                          ? dictConfig.fields.map((field) => (
                              <td key={field.name}>{entry[field.name] || '—'}</td>
                            ))
                          : <td className="title-cell">{entry.name}</td>}
                        {dictConfig ? (
                          <td>
                            <div className="row-actions">
                              <button
                                type="button"
                                className="retro-btn retro-btn--small"
                                onClick={() => openEdit(entry)}
                              >
                                <Icon name="edit" size={12} /> Изменить
                              </button>
                              <button
                                type="button"
                                className="retro-btn retro-btn--small danger"
                                onClick={() => setDeleteTarget(entry)}
                              >
                                <Icon name="trash" size={12} /> Удалить
                              </button>
                            </div>
                          </td>
                        ) : null}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          ) : null}
        </section>
      ) : null}

      {tab === 'audit' ? (
        <section className="retro-panel panel">
          <div className="section-title">
            <span>
              <Icon name="clock" size={14} /> Журнал аудита
            </span>
            <span className="section-title__meta">Последние действия пользователей</span>
          </div>

          {auditError ? <div className="form-error">{auditError}</div> : null}
          {auditLoading ? <div className="loading-state">Загрузка журнала…</div> : null}

          {!auditLoading && !auditError ? (
            audit.content.length === 0 ? (
              <div className="empty-state">
                <strong>Записей нет</strong>
                Журнал аудита пока пуст.
              </div>
            ) : (
              <>
                <div className="table-wrap">
                  <table className="retro-table">
                    <thead>
                      <tr>
                        <th>Дата и время</th>
                        <th>Пользователь</th>
                        <th>Действие</th>
                        <th>Объект</th>
                        <th>Идентификатор</th>
                      </tr>
                    </thead>
                    <tbody>
                      {audit.content.map((entry) => (
                        <tr key={entry.id}>
                          <td className="nowrap">{formatDateTime(entry.changedAt)}</td>
                          <td>{entry.userEmail || '—'}</td>
                          <td><span className="badge neutral">{entry.actionName || '—'}</span></td>
                          <td>{entry.entityType || '—'}</td>
                          <td className="nowrap"><code>{entry.entityId ? String(entry.entityId).slice(0, 8) : '—'}</code></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="catalog-footer">
                  <span className="hint">{audit.totalElements} записей</span>
                  <Pagination
                    page={auditPage}
                    totalPages={audit.totalPages}
                    totalElements={audit.totalElements}
                    onChange={setAuditPage}
                  />
                </div>
              </>
            )
          ) : null}
        </section>
      ) : null}

      {editTarget ? (
        <Dialog
          title={`Изменить запись: ${dictConfig?.label || ''}`}
          icon="edit"
          onClose={() => setEditTarget(null)}
          footer={
            <>
              <button
                type="button"
                className="retro-btn"
                onClick={() => setEditTarget(null)}
                disabled={savingEntry}
              >
                Отмена
              </button>
              <button
                type="button"
                className="retro-btn primary"
                onClick={submitEdit}
                disabled={savingEntry}
              >
                {savingEntry ? 'Сохранение…' : 'Сохранить'}
              </button>
            </>
          }
        >
          {dictConfig?.fields.map((field) => (
            <div className="form-group" key={field.name}>
              <label htmlFor={`dict-edit-${field.name}`}>
                {field.label} {field.required ? <span className="required">*</span> : null}
              </label>
              <input
                id={`dict-edit-${field.name}`}
                className="retro-input"
                value={editForm[field.name] ?? ''}
                onChange={(event) =>
                  setEditForm((current) => ({ ...current, [field.name]: event.target.value }))
                }
              />
            </div>
          ))}
        </Dialog>
      ) : null}

      {deleteTarget && dictConfig ? (
        <ConfirmDialog
          title="Удаление записи справочника"
          danger
          busy={deleting}
          message={`Удалить запись «${entryTitle(deleteTarget)}»? Если она используется в данных, сервер запретит удаление.`}
          confirmLabel="Удалить"
          onCancel={() => setDeleteTarget(null)}
          onConfirm={confirmDelete}
        />
      ) : null}
    </div>
  );
};

export default AdminScreen;
