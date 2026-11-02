import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { createCopy, deleteCopy, getCopies, updateCopyStatus } from '../api/copies';
import { getItemStatuses } from '../api/dictionaries';
import { issueByCopy } from '../api/loans';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatMoney } from '../utils/format';
import { itemStatusLabel, statusTone } from '../utils/labels';
import ConfirmDialog from './ConfirmDialog';
import Icon from './Icon';
import ReaderPicker from './ReaderPicker';

const emptyForm = {
  inventoryNumber: '',
  acquisitionDate: '',
  price: '',
};

const CopiesPanel = ({ itemId, canManage = false, canDelete = false }) => {
  const [copies, setCopies] = useState([]);
  const [statuses, setStatuses] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [reader, setReader] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadCopies = useCallback(async () => {
    if (!itemId) return;
    setLoading(true);
    setError('');

    try {
      const response = await getCopies(itemId);
      setCopies(Array.isArray(response.data) ? response.data : []);
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить экземпляры'));
    } finally {
      setLoading(false);
    }
  }, [itemId]);

  useEffect(() => {
    loadCopies();
  }, [loadCopies]);

  useEffect(() => {
    getItemStatuses()
      .then((response) => setStatuses(response.data || []))
      .catch(() => setStatuses([]));
  }, []);

  const available = copies.filter((copy) => copy.statusName === 'AVAILABLE').length;
  const issued = copies.filter((copy) => copy.statusName === 'BORROWED').length;

  const handleAdd = async (event) => {
    event.preventDefault();
    const inventoryNumber = form.inventoryNumber.trim();

    if (!inventoryNumber) {
      toast.warning('Введите инвентарный номер');
      return;
    }

    setSaving(true);

    try {
      await createCopy({
        libraryItemId: itemId,
        inventoryNumber,
        acquisitionDate: form.acquisitionDate || null,
        price: form.price === '' ? null : Number(form.price),
      });
      toast.success('Экземпляр добавлен');
      setForm(emptyForm);
      await loadCopies();
    } catch (saveError) {
      toast.error(getErrorMessage(saveError, 'Не удалось добавить экземпляр'));
    } finally {
      setSaving(false);
    }
  };

  const handleStatus = async (copy, status) => {
    if (!status || status === copy.statusName) return;

    setBusyId(copy.id);
    try {
      await updateCopyStatus(copy.id, status);
      toast.success('Статус экземпляра обновлён');
      await loadCopies();
    } catch (statusError) {
      toast.error(getErrorMessage(statusError, 'Не удалось изменить статус'));
    } finally {
      setBusyId(null);
    }
  };

  const handleIssue = async (copy) => {
    if (!reader) {
      toast.warning('Сначала выберите читателя');
      return;
    }

    setBusyId(copy.id);
    try {
      await issueByCopy(reader.id, copy.id);
      toast.success(`Экземпляр ${copy.inventoryNumber} выдан: ${reader.fullName}`);
      await loadCopies();
    } catch (issueError) {
      toast.error(getErrorMessage(issueError, 'Не удалось выдать экземпляр'));
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;

    setDeleting(true);
    try {
      await deleteCopy(deleteTarget.id);
      toast.success('Экземпляр удалён');
      setDeleteTarget(null);
      await loadCopies();
    } catch (deleteError) {
      toast.error(getErrorMessage(deleteError, 'Не удалось удалить экземпляр'));
    } finally {
      setDeleting(false);
    }
  };

  const changeableStatuses = statuses.filter((status) => status.name !== 'BORROWED');

  return (
    <section>
      <div className="section-title">
        <span>
          <Icon name="box" size={14} /> Экземпляры
        </span>
        <span className="section-title__meta">
          всего: {copies.length} · доступно: {available} · выдано: {issued}
        </span>
      </div>

      {canManage ? (
        <>
          <form className="copy-form" onSubmit={handleAdd}>
            <div className="form-group form-group--inv">
              <label htmlFor={`inv-${itemId}`}>Инвентарный номер</label>
              <input
                id={`inv-${itemId}`}
                value={form.inventoryNumber}
                onChange={(event) =>
                  setForm((current) => ({ ...current, inventoryNumber: event.target.value }))
                }
                placeholder="Например, INV-001"
                maxLength={64}
              />
            </div>
            <div className="form-group">
              <label htmlFor={`acq-${itemId}`}>Дата поступления</label>
              <input
                id={`acq-${itemId}`}
                type="date"
                value={form.acquisitionDate}
                onChange={(event) =>
                  setForm((current) => ({ ...current, acquisitionDate: event.target.value }))
                }
              />
            </div>
            <div className="form-group">
              <label htmlFor={`price-${itemId}`}>Цена, ₽</label>
              <input
                id={`price-${itemId}`}
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={(event) =>
                  setForm((current) => ({ ...current, price: event.target.value }))
                }
                placeholder="0.00"
              />
            </div>
            <button type="submit" className="retro-btn" disabled={saving}>
              <Icon name="plus" size={13} />
              {saving ? 'Добавление…' : 'Добавить'}
            </button>
          </form>

          <div className="copy-issue">
            <div className="copy-issue__label">
              <strong>Читатель для выдачи</strong>
              <span className="hint">Выберите читателя, затем нажмите «Выдать» у свободного экземпляра.</span>
            </div>
            <ReaderPicker value={reader} onChange={setReader} />
          </div>
        </>
      ) : null}

      {error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={loadCopies}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : loading ? (
        <div className="loading-state">Загрузка экземпляров…</div>
      ) : copies.length === 0 ? (
        <div className="empty-state">
          <strong>Экземпляров пока нет</strong>
          {canManage ? 'Добавьте первый экземпляр в форме выше.' : 'Библиотекарь ещё не добавил экземпляры.'}
        </div>
      ) : (
        <div className="table-wrap">
          <table className="retro-table">
            <thead>
              <tr>
                <th>Инв. номер</th>
                <th>Статус</th>
                <th>Поступил</th>
                <th>Цена</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {copies.map((copy) => {
                const busy = busyId === copy.id;
                const isAvailable = copy.statusName === 'AVAILABLE';

                return (
                  <tr key={copy.id}>
                    <td className="title-cell">{copy.inventoryNumber}</td>
                    <td>
                      <span className={`badge ${statusTone(copy.statusName)}`}>
                        {itemStatusLabel(copy.statusName)}
                      </span>
                    </td>
                    <td className="nowrap">{formatDate(copy.acquisitionDate)}</td>
                    <td className="num nowrap">{formatMoney(copy.price)}</td>
                    <td>
                      <div className="row-actions">
                        {canManage ? (
                          <select
                            value=""
                            onChange={(event) => handleStatus(copy, event.target.value)}
                            disabled={busy || copy.statusName === 'BORROWED'}
                            title={
                              copy.statusName === 'BORROWED'
                                ? 'Для выданного экземпляра статус меняется только через возврат'
                                : 'Изменить статус'
                            }
                          >
                            <option value="">Статус…</option>
                            {changeableStatuses.map((status) => (
                              <option key={status.id} value={status.name}>
                                {itemStatusLabel(status.name)}
                              </option>
                            ))}
                          </select>
                        ) : null}

                        {canManage && isAvailable ? (
                          <button
                            type="button"
                            className="retro-btn retro-btn--small success"
                            onClick={() => handleIssue(copy)}
                            disabled={busy || !reader}
                            title={reader ? 'Выдать выбранному читателю' : 'Сначала выберите читателя'}
                          >
                            Выдать
                          </button>
                        ) : null}

                        {canDelete ? (
                          <button
                            type="button"
                            className="retro-btn retro-btn--small danger"
                            onClick={() => setDeleteTarget(copy)}
                            disabled={busy}
                            title="Удалить экземпляр"
                          >
                            <Icon name="trash" size={12} />
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {deleteTarget ? (
        <ConfirmDialog
          title="Удаление экземпляра"
          message={`Удалить экземпляр ${deleteTarget.inventoryNumber}?`}
          confirmLabel="Удалить"
          danger
          busy={deleting}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      ) : null}
    </section>
  );
};

export default CopiesPanel;
