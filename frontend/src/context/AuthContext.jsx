import { createContext, useContext, useEffect, useState } from 'react';
import { login as apiLogin } from '../api/auth';

const AuthContext = createContext(null);

const decodeToken = (token) => {
  try {
    const payloadPart = token.split('.')[1];
    if (!payloadPart) return null;

    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const binary = window.atob(padded);
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
    const payload = JSON.parse(new TextDecoder().decode(bytes));

    if (payload.exp && payload.exp * 1000 <= Date.now()) return null;
    return payload;
  } catch {
    return null;
  }
};

const readStoredAuth = () => {
  const storedToken = localStorage.getItem('token');

  if (!storedToken) {
    return { token: null, user: null };
  }

  const payload = decodeToken(storedToken);

  if (!payload) {
    localStorage.removeItem('token');
    return { token: null, user: null };
  }

  return {
    token: storedToken,
    user: {
      email: payload.sub,
      role: payload.role,
    },
  };
};

export const AuthProvider = ({ children }) => {
  const [auth, setAuth] = useState(readStoredAuth);

  useEffect(() => {
    const handleStorage = (event) => {
      if (event.key === 'token') {
        setAuth(readStoredAuth());
      }
    };

    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  const login = async (email, password) => {
    const { data } = await apiLogin(email, password);
    const payload = decodeToken(data.token);

    if (!payload) {
      throw new Error('Сервер вернул некорректный токен');
    }

    localStorage.setItem('token', data.token);
    setAuth({
      token: data.token,
      user: {
        email: payload.sub,
        role: payload.role,
      },
    });
  };

  const logout = () => {
    localStorage.removeItem('token');
    setAuth({ token: null, user: null });
  };

  return (
    <AuthContext.Provider
      value={{
        user: auth.user,
        token: auth.token,
        isAuthenticated: Boolean(auth.user),
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth должен использоваться внутри AuthProvider');
  }

  return context;
};
