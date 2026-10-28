import { Link } from 'react-router-dom';

const NotFound = () => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100vh',
      textAlign: 'center',
      padding: '20px'
    }}>
      <h1 style={{ fontSize: '72px', margin: 0 }}>404</h1>
      <h2>Страница не найдена</h2>
      <p>Извините, запрошенная страница не существует.</p>
      <Link to="/" style={{ marginTop: '20px' }}>Вернуться на главную</Link>
    </div>
  );
};

export default NotFound;