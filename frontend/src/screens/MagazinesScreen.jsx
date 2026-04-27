import { useState, useEffect } from 'react';
import { getMagazines, deleteMagazine } from '../api/magazines';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const MagazinesScreen = () => {
  const [magazines, setMagazines] = useState([]);
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    getMagazines(page).then(res => setMagazines(res.data.content));
  }, [page]);

  const handleDelete = async (id) => {
    await deleteMagazine(id);
    setMagazines(prev => prev.filter(m => m.id !== id));
  };

  return (
    <div>
      <h2>Журналы</h2>
      <button onClick={() => navigate('/magazines/new')}>Добавить</button>
      <table>
        <thead><tr><th>Название</th><th>Жанр</th><th></th></tr></thead>
        <tbody>
          {magazines.map(m => (
            <tr key={m.id}>
              <td>{m.title}</td>
              <td>{m.genre}</td>
              <td>
                <button onClick={() => navigate(`/magazines/${m.id}`)}>Экземпляры</button>
                {user?.role === 'ROLE_ADMIN' && <button onClick={() => handleDelete(m.id)}>Удалить</button>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default MagazinesScreen;