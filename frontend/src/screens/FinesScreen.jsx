import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { getFines, payFine } from '../api/fines';
import { getErrorMessage } from '../utils/errors';
import { formatDate, formatMoney } from '../utils/format';
import ConfirmDialog from '../components/ConfirmDialog';
import Icon from '../components/Icon';
import PageHeader from '../components/PageHeader';

const FILTERS = [
  { key: 'unpaid', label: 'Неоплаченные', value: false },
  { key: 'all', label: 'Все', value: null },
  { key: 'paid', label: 'Оплаченные', value: true },
];

const FinesScreen = () => {
  const [filter, setFilter] = useState('unpaid');
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [payTarget, setPayTarget] = useState(null);
  const [paying, setPaying] = useState(false);

  const loadFines = useCallback(async () => {
    setLoading(true);
    setError('');

    const paidValue = FILTERS.find((item) => item.key === filter)?.value ?? null;

    try {
      const response = await getFines(paidValue);
      setFines(Array.isArray(response.data) ? response.data : []);
    } catch (loadError) {
      setError(getErrorMessage(loadError, 'Не удалось загрузить штрафы'));
      setFines([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadFines();
  }, [loadFines, reloadKey]);

  const reload = () => setReloadKey((key) => key + 1);

  const confirmPay = async () => {
    if (!payTarget) return;
    setPaying(true);

    try {
      await payFine(payTarget.id);
      toast.success('Штраф отмечен как оплаченный');
      setPayTarget(null);
      reload();
    } catch (payError) {
      toast.error(getErrorMessage(payError, 'Не удалось отметить штраф оплаченным'));
    } finally {
      setPaying(false);
    }
  };

  const unpaidTotal = fines
    .filter((fine) => !fine.isPaid)
    .reduce((sum, fine) => sum + (Number(fine.amount) || 0), 0);

  return (
    <div className="screen">
      <PageHeader
        title="Штрафы"
        subtitle="Задолженности читателей и их оплата"
        icon="warning"
        actions={
          <button type="button" className="retro-btn" onClick={reload}>
            <Icon name="refresh" size={13} /> Обновить
          </button>
        }
      />

      <div className="filter-bar">
        <div className="filter-actions">
          {FILTERS.map((item) => (
            <button
              key={item.key}
              type="button"
              className={`retro-btn${filter === item.key ? ' primary' : ''}`}
              onClick={() => setFilter(item.key)}
            >
              {item.label}
            </button>
          ))}
        </div>
        {filter !== 'paid' && unpaidTotal > 0 ? (
          <span className="hint">Итого неоплаченных: {formatMoney(unpaidTotal)}</span>
        ) : null}
      </div>

      {loading ? (
        <div className="loading-state">Загрузка штрафов…</div>
      ) : error ? (
        <div className="error-state">
          <strong>Ошибка загрузки</strong>
          {error}
          <div className="form-actions">
            <button type="button" className="retro-btn retro-btn--small" onClick={reload}>
              <Icon name="refresh" size={13} /> Повторить
            </button>
          </div>
        </div>
      ) : fines.length === 0 ? (
        <div className="empty-state">
          <strong>Штрафов нет</strong>
          Задолженности появятся при просрочке возвратов.
        </div>
      ) : (
        <div className="table-wrap">
          <table className="retro-table">
            <thead>
              <tr>
                <th>Читатель</th>
                <th>Издание</th>
                <th>Инв. номер</th>
                <th>Причина</th>
                <th>Срок возврата</th>
                <th>Сумма</th>
                <th>Статус</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {fines.map((fine) => (
                <tr key={fine.id}>
                  <td className="title-cell">{fine.userFullName || '—'}</td>
                  <td>{fine.bookTitle || '—'}</td>
                  <td className="nowrap">{fine.inventoryNumber || '—'}</td>
                  <td>{fine.reason || '—'}</td>
                  <td className="nowrap">{formatDate(fine.dueDate)}</td>
                  <td className="num">{formatMoney(fine.amount)}</td>
                  <td>
                    <span className={`badge ${fine.isPaid ? 'success' : 'warning'}`}>
                      {fine.isPaid ? 'Оплачен' : 'Не оплачен'}
                    </span>
                  </td>
                  <td>
                    {fine.isPaid ? (
                      <span className="hint">—</span>
                    ) : (
                      <button
                        type="button"
                        className="retro-btn retro-btn--small success"
                        onClick={() => setPayTarget(fine)}
                      >
                        Отметить оплаченным
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {payTarget ? (
        <ConfirmDialog
          title="Оплата штрафа"
          message={`Отметить штраф ${formatMoney(payTarget.amount)} («${payTarget.reason || 'без причины'}», ${payTarget.userFullName || 'читатель'}) как оплаченный?`}
          confirmLabel="Оплачен"
          onConfirm={confirmPay}
          onCancel={() => setPayTarget(null)}
          busy={paying}
        />
      ) : null}
    </div>
  );
};

export default FinesScreen;
