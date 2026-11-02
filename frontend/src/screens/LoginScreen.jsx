import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';
import Icon from '../components/Icon';

const LoginScreen = () => {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!email.trim() || !password) {
      setError('Введите email и пароль');
      return;
    }

    setLoading(true);

    try {
      await login(email.trim(), password);
      toast.success('Вход выполнен');
      navigate(location.state?.from || '/', { replace: true });
    } catch (loginError) {
      setError(getErrorMessage(loginError, 'Не удалось войти. Проверьте email и пароль.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-shell">
      <div className="login-window">
        <div className="retro-titlebar">
          <span className="titlebar-icon">
            <Icon name="book" size={14} />
          </span>
          <span className="titlebar-title">Вход в LIMS</span>
          <div className="window-controls">
            <span className="window-control" aria-hidden="true">_</span>
            <span className="window-control" aria-hidden="true">▢</span>
            <span className="window-control window-control--close" aria-hidden="true">✕</span>
          </div>
        </div>

        <div className="login-body">
          <div className="login-banner">
            <div className="login-banner__icon">
              <Icon name="book" size={30} />
            </div>
            <div>
              <h1 className="login-banner__title">Библиотека LIMS</h1>
              <p className="login-banner__subtitle">Информационная система библиотеки · вход для сотрудников</p>
            </div>
          </div>

          {error ? <div className="form-error" role="alert">{error}</div> : null}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="login-email">Email</label>
              <input
                id="login-email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="admin@lims.com"
                autoComplete="username"
                autoFocus
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="login-password">Пароль</label>
              <input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Введите пароль"
                autoComplete="current-password"
                disabled={loading}
              />
            </div>

            <label className="login-checkbox">
              <input
                type="checkbox"
                checked={showPassword}
                onChange={(event) => setShowPassword(event.target.checked)}
              />
              Показать пароль
            </label>

            <div className="form-actions form-actions--end">
              <button type="submit" className="retro-btn primary" disabled={loading}>
                <Icon name="user" size={14} />
                {loading ? 'Вход…' : 'Войти'}
              </button>
            </div>
          </form>

          <div className="demo-box">
            <div className="demo-line">
              <span>Администратор:</span>
              <code>admin@lims.com / admin123</code>
            </div>
            <div className="demo-line">
              <span>Библиотекарь:</span>
              <code>librarian@lims.com / librarian123</code>
            </div>
          </div>
        </div>

        <div className="statusbar">
          <span className="status-cell status-cell--grow">Готово</span>
          <span className="status-cell">LIMS 1.0</span>
        </div>
      </div>
    </div>
  );
};

export default LoginScreen;
