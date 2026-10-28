import { useState, useEffect } from 'react';
import { getReaders, createReader, changeUserStatus, updateMaxLoans, searchReaders } from '../api/readers';
import { toast } from 'react-toastify';

const ReadersScreen = () => {
  const [readers, setReaders] = useState([]);
  const [search, setSearch] = useState('');
  const [newUser, setNewUser] = useState({ firstName: '', lastName: '', email: '' });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchReaders = async () => {
    setLoading(true);
    try {
      let res;
      if (search.trim()) {
        res = await searchReaders(search);
        setReaders(res.data);
        setTotalPages(1);
      } else {
        res = await getReaders(page, 10);
        setReaders(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      toast.error('Ошибка загрузки читателей');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchReaders(); }, [page, search]);

  const handleCreate = async () => {
    if (!newUser.firstName || !newUser.lastName || !newUser.email) {
      toast.warning('Заполните все поля');
      return;
    }
    try {
      await createReader({ ...newUser, role: 'READER' });
      toast.success('Читатель создан');
      setNewUser({ firstName: '', lastName: '', email: '' });
      fetchReaders();
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleStatus = async (id, status) => {
    try {
      await changeUserStatus(id, status);
      toast.success('Статус обновлён');
      fetchReaders();
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleMaxLoans = async (id, max) => {
    try {
      await updateMaxLoans(id, max);
      toast.success('Лимит обновлён');
      fetchReaders();
    } catch (err) {
      toast.error('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div>
      <h2>👥 Читатели</h2>

      <div className="search-bar">
        <input
          placeholder="Поиск по имени или email..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <h3>Зарегистрировать нового</h3>
        <div className="search-bar">
          <input placeholder="Имя" value={newUser.firstName} onChange={e => setNewUser({...newUser, firstName: e.target.value})} />
          <input placeholder="Фамилия" value={newUser.lastName} onChange={e => setNewUser({...newUser, lastName: e.target.value})} />
          <input placeholder="Email" value={newUser.email} onChange={e => setNewUser({...newUser, email: e.target.value})} />
          <button onClick={handleCreate}>Создать</button>
        </div>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>ФИО</th>
              <th>Email</th>
              <th>Статус</th>
              <th>Взято сейчас</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {readers.map(r => (
              <tr key={r.id}>
                <td>{r.fullName}</td>
                <td>{r.email}</td>
                <td>
                  <span className={`badge ${r.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
                    {r.status || 'ACTIVE'}
                  </span>
                </td>
                <td style={{ textAlign: 'center' }}>{r.activeLoansCount ?? 0}</td>
                <td>
                  <button className="warning" onClick={() => handleStatus(r.id, 'FROZEN')}>Заморозить</button>
                  <button className="success" onClick={() => handleStatus(r.id, 'ACTIVE')}>Активировать</button>
                  <input
                    type="number"
                    defaultValue={r.maxActiveLoans}
                    onBlur={(e) => handleMaxLoans(r.id, parseInt(e.target.value))}
                    style={{ width: '60px', marginLeft: '5px' }}
                    title="Лимит выдачи"
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <p>Загрузка...</p>}
      </div>

      <div style={{
        display: 'flex',
        justifyContent: 'center',
        gap: '10px',
        marginTop: '20px'
      }}>
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Назад</button>
        <span style={{ alignSelf: 'center' }}>Страница {page + 1} из {totalPages}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(p => p + 1)}>Вперёд →</button>
      </div>
    </div>
  );
};

export default ReadersScreen;