import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Layout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm('Вы уверены, что хотите выйти?')) {
      logout();
      navigate('/login');
    }
  };

  if (!user) return <Outlet />;

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <nav style={{
        width: '240px',
        background: 'var(--bg-sidebar)',
        color: 'var(--text-white)',
        padding: '20px 0',
        boxShadow: '2px 0 10px rgba(0,0,0,0.1)',
        position: 'fixed',
        top: 0,
        left: 0,
        bottom: 0,
        overflowY: 'auto',
        zIndex: 100
      }}>
        <div style={{ padding: '0 20px 20px', borderBottom: '1px solid rgba(255,255,255,0.1)', marginBottom: '20px' }}>
          <h2 style={{ color: 'white', margin: '0 0 10px 0', border: 'none', fontSize: '22px' }}>📚 LIMS</h2>
          <p style={{ fontSize: '13px', color: 'rgba(255,255,255,0.7)', margin: 0 }}>
            {user.email}
          </p>
          <span className="badge" style={{
            background: user.role === 'ROLE_ADMIN' ? '#e74c3c' : '#3498db',
            color: 'white',
            marginTop: '8px',
            display: 'inline-block'
          }}>
            {user.role === 'ROLE_ADMIN' ? 'Админ' : 'Библиотекарь'}
          </span>
        </div>

        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    <MenuItem to="/" icon="🏠">Главная</MenuItem>
                    <MenuItem to="/catalog" icon="📚">Каталог</MenuItem>
                    <MenuItem to="/readers" icon="👥">Читатели</MenuItem>
                    <MenuItem to="/loans" icon="🔄">Выдачи</MenuItem>
                    {user.role === 'ROLE_ADMIN' && (
                      <MenuItem to="/statistics" icon="📊">Статистика</MenuItem>
                    )}
                  </ul>

        <div style={{ padding: '20px', borderTop: '1px solid rgba(255,255,255,0.1)', marginTop: 'auto' }}>
          <button
            onClick={handleLogout}
            style={{
              width: '100%',
              background: 'rgba(255,255,255,0.1)',
              color: 'white',
              border: 'none',
              padding: '10px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            🚪 Выйти
          </button>
        </div>
      </nav>

      {/* Main content */}
      <main style={{ flex: 1, marginLeft: '240px', padding: '30px', background: 'var(--bg)', minHeight: '100vh' }}>
        <Outlet />
      </main>
    </div>
  );
};

const MenuItem = ({ to, icon, children }) => (
  <li>
    <Link
      to={to}
      style={{
        display: 'flex',
        alignItems: 'center',
        padding: '12px 20px',
        color: 'rgba(255,255,255,0.8)',
        textDecoration: 'none',
        transition: 'all 0.2s',
        borderLeft: '3px solid transparent',
        fontSize: '15px'
      }}
      onMouseEnter={(e) => {
        e.target.style.background = 'rgba(255,255,255,0.1)';
        e.target.style.color = 'white';
        e.target.style.borderLeftColor = '#3498db';
      }}
      onMouseLeave={(e) => {
        e.target.style.background = 'transparent';
        e.target.style.color = 'rgba(255,255,255,0.8)';
        e.target.style.borderLeftColor = 'transparent';
      }}
    >
      <span style={{ marginRight: '10px' }}>{icon}</span>
      {children}
    </Link>
  </li>
);

export default Layout;