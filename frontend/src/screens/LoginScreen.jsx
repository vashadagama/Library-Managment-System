import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const LoginScreen = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      alert('Ошибка входа: ' + err.response?.data?.message || err.message);
    }
  };

  return (
    <div style={{ padding: 40 }}>
      <h2>Вход в систему</h2>
      <form onSubmit={handleSubmit}>
        <div><input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} /></div>
        <div><input type="password" placeholder="Пароль" value={password} onChange={e => setPassword(e.target.value)} /></div>
        <div><button type="submit">Войти</button></div>
      </form>
    </div>
  );
};

export default LoginScreen;