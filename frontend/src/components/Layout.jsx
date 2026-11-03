import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { userRoleLabel } from '../utils/labels';
import Icon from './Icon';

const navItems = [
  { to: '/', label: 'Главная', icon: 'home', end: true },
  { to: '/catalog', label: 'Каталог', icon: 'book' },
  { to: '/authors', label: 'Авторы', icon: 'authors' },
  { to: '/readers', label: 'Читатели', icon: 'readers' },
  { to: '/loans', label: 'Выдачи', icon: 'loans' },
  { to: '/statistics', label: 'Статистика', icon: 'statistics' },
];

const Layout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const [maximized, setMaximized] = useState(false);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 30000);
    return () => window.clearInterval(timer);
  }, []);

  const handleLogout = () => {
    if (!window.confirm('Выйти из системы?')) return;
    logout();
    navigate('/login', { replace: true });
  };

  if (!user) return <Outlet />;

  const role = userRoleLabel(user.role?.replace('ROLE_', ''));
  const isHome = location.pathname === '/';

  return (
    <div className={`app-shell${maximized ? ' app-shell--max' : ''}`}>
      <div
        className={`app-window retro-panel${maximized ? ' app-window--max' : ''}${
          collapsed ? ' app-window--collapsed' : ''
        }`}
      >
        <div className="retro-titlebar">
          <span className="titlebar-icon">
            <Icon name="book" size={14} />
          </span>
          <span className="titlebar-title">
            LIMS — Библиотечная информационная система
          </span>
          <div className="window-controls">
            <button
              type="button"
              className="window-control"
              onClick={() => setCollapsed((value) => !value)}
              title={collapsed ? 'Восстановить окно' : 'Свернуть окно'}
              aria-label={collapsed ? 'Восстановить окно' : 'Свернуть окно'}
            >
              {collapsed ? '▣' : '_'}
            </button>
            <button
              type="button"
              className="window-control"
              onClick={() => setMaximized((value) => !value)}
              title={maximized ? 'Восстановить размер' : 'Развернуть окно'}
              aria-label={maximized ? 'Восстановить размер' : 'Развернуть окно'}
            >
              {maximized ? '❐' : '▢'}
            </button>
            <button
              type="button"
              className="window-control window-control--close"
              onClick={handleLogout}
              title="Выйти из системы"
              aria-label="Выйти из системы"
            >
              ✕
            </button>
          </div>
        </div>

        <nav className="app-menubar" aria-label="Основная навигация">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `menu-link${isActive ? ' active' : ''}`}
            >
              <Icon name={item.icon} size={14} />
              {item.label}
            </NavLink>
          ))}
          <span className="menu-spacer" />
          <span className="menu-user" title={user.email}>
            <Icon name="user" size={13} />
            <strong>{user.email}</strong>
            <span className={`badge ${user.role === 'ROLE_ADMIN' ? 'info' : 'warning'}`}>
              {role}
            </span>
          </span>
          <button type="button" className="menu-link" onClick={handleLogout}>
            <Icon name="logout" size={14} />
            Выход
          </button>
        </nav>

        <div className="app-toolbar">
          <button
            type="button"
            className="retro-btn retro-btn--small"
            onClick={() => navigate('/books/new')}
          >
            <Icon name="plus" size={13} />
            Новая книга
          </button>
          <button
            type="button"
            className="retro-btn retro-btn--small"
            onClick={() => navigate('/magazines/new')}
          >
            <Icon name="plus" size={13} />
            Новый журнал
          </button>
          <span className="toolbar-separator" />
          <button
            type="button"
            className="retro-btn retro-btn--small"
            onClick={() => navigate('/catalog')}
          >
            <Icon name="loans" size={13} />
            Оформить выдачу
          </button>
          <button
            type="button"
            className="retro-btn retro-btn--small"
            onClick={() => navigate('/loans')}
          >
            <Icon name="clock" size={13} />
            Просрочки
          </button>
          <span className="toolbar-spacer" />
          <span className="toolbar-label">
            {isHome ? 'Рабочий стол' : 'Система работает в штатном режиме'}
          </span>
        </div>

        <div className="app-body">
          <main className="app-content">
            <Outlet />
          </main>
        </div>

        <div className="statusbar">
          <span className="status-cell status-cell--grow">Готово</span>
          <span className="status-cell">{role}</span>
          <span className="status-cell">{user.email}</span>
          <span className="status-cell">
            {now.toLocaleDateString('ru-RU')} {now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
          </span>
          <span className="status-cell">LIMS 1.0</span>
        </div>
      </div>
    </div>
  );
};

export default Layout;
