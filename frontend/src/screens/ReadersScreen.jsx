import { useState, useEffect } from 'react';
import { getReaders, createReader, changeUserStatus, updateMaxLoans, searchReaders } from '../api/readers';

const ReadersScreen = () => {
  const [readers, setReaders] = useState([]);
  const [search, setSearch] = useState('');
  const [newUser, setNewUser] = useState({ firstName: '', lastName: '', email: '' });

  const fetchReaders = () => {
    const apiCall = search ? searchReaders(search) : getReaders();
    apiCall.then(res => setReaders(res.data)).catch(err => console.error(err));
  };

  useEffect(() => { fetchReaders(); }, [search]);

  const handleCreate = async () => {
    await createReader({ ...newUser, role: 'READER' });
    setNewUser({ firstName: '', lastName: '', email: '' });
    fetchReaders();
  };

  const handleStatus = async (id, status) => {
    await changeUserStatus(id, status);
    fetchReaders();
  };

  const handleMaxLoans = async (id, max) => {
    await updateMaxLoans(id, max);
    fetchReaders();
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
      </div>
    </div>
  );
};

export default ReadersScreen;