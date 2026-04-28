import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const LoginScreen = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!email || !password) {
      setError('Заполните все поля');
      return;
    }
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка входа. Проверьте данные.');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%)'
    }}>
      <div className="card" style={{
        width: '400px',
        padding: '40px'
      }}>
        <h2 style={{ textAlign: 'center', border: 'none', marginBottom: '30px' }}>
          📚 Библиотека LIMS
        </h2>
        <p style={{ textAlign: 'center', color: 'var(--text-light)', marginBottom: '30px' }}>
          Вход для сотрудников
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="Введите email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              style={{ width: '100%' }}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label>Пароль</label>
            <input
              type="password"
              placeholder="Введите пароль"
              value={password}
              onChange={e => setPassword(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>

          <button type="submit" style={{
            width: '100%',
            padding: '12px',
            fontSize: '16px',
            marginTop: '20px'
          }}>
            Войти
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginScreen;