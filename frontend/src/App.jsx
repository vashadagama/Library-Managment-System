import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import LoginScreen from './screens/LoginScreen';
import Dashboard from './screens/Dashboard';
import CatalogScreen from './screens/CatalogScreen'; // новый
import BookDetailScreen from './screens/BookDetailScreen';
import MagazineDetailScreen from './screens/MagazineDetailScreen';
import ReadersScreen from './screens/ReadersScreen';
import LoansScreen from './screens/LoansScreen';
import StatisticsScreen from './screens/StatisticsScreen';

const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginScreen />} />
          <Route
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route path="/" element={<Dashboard />} />
            <Route path="/catalog" element={<CatalogScreen />} />
            <Route path="/books/:id" element={<BookDetailScreen />} />
            <Route path="/magazines/:id" element={<MagazineDetailScreen />} />
            <Route path="/readers" element={<ReadersScreen />} />
            <Route path="/loans" element={<LoansScreen />} />
            <Route
              path="/statistics"
              element={
                <PrivateRoute roles={['ROLE_ADMIN']}>
                  <StatisticsScreen />
                </PrivateRoute>
              }
            />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;