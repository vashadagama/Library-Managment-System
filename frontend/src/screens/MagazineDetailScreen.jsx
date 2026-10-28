import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getMagazine, getMagazineCopies, addCopy, updateCopyStatus } from '../api/magazines';
import { issueByCopy } from '../api/loans';
import { toast } from 'react-toastify';

const MagazineDetailScreen = () => {
  const { id } = useParams();
  const [magazine, setMagazine] = useState(null);
  const [copies, setCopies] = useState([]);
  const [invNumber, setInvNumber] = useState('');
  const [userId, setUserId] = useState('');

  const fetchData = () => {
    getMagazine(id).then(res => setMagazine(res.data));
    getMagazineCopies(id).then(res => setCopies(res.data));
  };

  useEffect(() => { fetchData(); }, [id]);

  const handleAddCopy = async () => {
    if (!invNumber.trim()) {
      toast.warning('Введите инвентарный номер');
      return;
    }
    try {
      await addCopy({ libraryItemId: id, inventoryNumber: invNumber.trim() });
      toast.success('Экземпляр добавлен');
      setInvNumber('');
      fetchData();
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleStatusChange = async (copyId, status) => {
    try {
      await updateCopyStatus(copyId, status);
      toast.success('Статус обновлён');
      fetchData();
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleIssue = async (copyId) => {
    if (!userId.trim()) {
      toast.warning('Введите ID читателя');
      return;
    }
    try {
      await issueByCopy(userId.trim(), copyId);
      toast.success('Журнал выдан');
      fetchData();
      setUserId('');
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  if (!magazine) return <div>Загрузка...</div>;

  return (
    <div>
      <h2>{magazine.title}</h2>
      <p>Издатель: {magazine.publisher}</p>
      <p>ISSN: {magazine.issn} | Жанр: {magazine.genre}</p>

      <h3>Экземпляры</h3>
      <div>
        <input placeholder="Инв. номер" value={invNumber} onChange={e => setInvNumber(e.target.value)} />
        <button onClick={handleAddCopy}>Добавить экземпляр</button>
      </div>
      <table>
        <thead><tr><th>Инв.номер</th><th>Статус</th><th>Действия</th></tr></thead>
        <tbody>
          {copies.map(c => (
            <tr key={c.id}>
              <td>{c.inventoryNumber}</td>
              <td>{c.status}</td>
              <td>
                <select onChange={(e) => handleStatusChange(c.id, e.target.value)} defaultValue="">
                  <option value="" disabled>Изменить статус</option>
                  <option value="AVAILABLE">Доступно</option>
                  <option value="UNDER_REPAIR">На ремонте</option>
                  <option value="LOST">Утеряно</option>
                </select>
                {c.status === 'AVAILABLE' && (
                  <div>
                    <input placeholder="ID читателя" value={userId} onChange={e => setUserId(e.target.value)} />
                    <button onClick={() => handleIssue(c.id)}>Выдать</button>
                  </div>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default MagazineDetailScreen;