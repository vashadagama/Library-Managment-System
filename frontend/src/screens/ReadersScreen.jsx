import { useState, useEffect } from 'react';
import { getReaders, createReader, changeUserStatus, updateMaxLoans } from '../api/readers';

const ReadersScreen = () => {
  const [readers, setReaders] = useState([]);
  const [newUser, setNewUser] = useState({ firstName: '', lastName: '', email: '' });

  const fetchReaders = () => getReaders().then(res => setReaders(res.data));
  useEffect(() => { fetchReaders(); }, []);

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
      <h2>Читатели</h2>
      <div>
        <h3>Зарегистрировать нового</h3>
        <input placeholder="Имя" value={newUser.firstName} onChange={e => setNewUser({...newUser, firstName: e.target.value})} />
        <input placeholder="Фамилия" value={newUser.lastName} onChange={e => setNewUser({...newUser, lastName: e.target.value})} />
        <input placeholder="Email" value={newUser.email} onChange={e => setNewUser({...newUser, email: e.target.value})} />
        <button onClick={handleCreate}>Создать</button>
      </div>
      <table>
        <thead><tr><th>ФИО</th><th>Email</th><th>Статус</th><th>Макс. книг</th><th>Действия</th></tr></thead>
        <tbody>
          {readers.map(r => (
            <tr key={r.id}>
              <td>{r.fullName}</td>
              <td>{r.email}</td>
              <td>{r.status || 'ACTIVE'}</td>
              <td>{r.maxActiveLoans}</td>
              <td>
                <button onClick={() => handleStatus(r.id, 'FROZEN')}>Заморозить</button>
                <button onClick={() => handleStatus(r.id, 'ACTIVE')}>Активировать</button>
                <input type="number" defaultValue={r.maxActiveLoans} onBlur={(e) => handleMaxLoans(r.id, parseInt(e.target.value))} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ReadersScreen;