import { Link, useNavigate } from 'react-router-dom';
import Icon from './Icon';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="login-shell">
      <div className="login-window">
        <div className="retro-titlebar">
          <span className="titlebar-icon"><Icon name="warning" size={14} /></span>
          <span className="titlebar-title">Ошибка 404</span>
          <div className="window-controls">
            <span className="window-control" aria-hidden="true">✕</span>
          </div>
        </div>
        <div className="notfound-body">
          <div className="notfound-code">404</div>
          <h1 className="notfound-title">Страница не найдена</h1>
          <p className="notfound-text">
            Запрошенный раздел не существует или был перемещён.
          </p>
          <div className="notfound-actions">
            <button type="button" className="retro-btn primary" onClick={() => navigate('/')}>
              <Icon name="home" size={13} /> На главную
            </button>
            <Link className="retro-btn" to="/catalog">
              <Icon name="book" size={13} /> В каталог
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
