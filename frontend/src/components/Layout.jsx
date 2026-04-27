import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Layout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <nav style={{ width: 200, background: '#ddd', padding: 10 }}>
        <h3>LIMS</h3>
        <p>{user?.email} ({user?.role})</p>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><Link to="/">Главная</Link></li>
          <li><Link to="/books">Книги</Link></li>
          <li><Link to="/magazines">Журналы</Link></li>
          <li><Link to="/readers">Читатели</Link></li>
          <li><Link to="/loans">Выдачи</Link></li>
          <li><Link to="/statistics">Статистика</Link></li>
        </ul>
        <button onClick={handleLogout}>Выйти</button>
      </nav>
      <main style={{ flex: 1, padding: 20 }}>
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;