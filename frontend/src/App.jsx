import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import LoginScreen from './screens/LoginScreen';
import Dashboard from './screens/Dashboard';
import CatalogScreen from './screens/CatalogScreen';
import BookDetailScreen from './screens/BookDetailScreen';
import MagazineDetailScreen from './screens/MagazineDetailScreen';
import ReadersScreen from './screens/ReadersScreen';
import LoansScreen from './screens/LoansScreen';
import StatisticsScreen from './screens/StatisticsScreen';
import IssuePage from './screens/IssuePage';
import BookForm from './components/BookForm';
import MagazineForm from './components/MagazineForm';

const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginScreen />} />
          <Route element={<PrivateRoute><Layout /></PrivateRoute>}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/catalog" element={<CatalogScreen />} />
            <Route path="/books/:id" element={<BookDetailScreen />} />
            <Route path="/magazines/:id" element={<MagazineDetailScreen />} />
            <Route path="/readers" element={<ReadersScreen />} />
            <Route path="/loans" element={<LoansScreen />} />
            <Route path="/statistics" element={<PrivateRoute roles={['ROLE_ADMIN']}><StatisticsScreen /></PrivateRoute>} />
            <Route path="/books/new" element={<BookForm />} />
            <Route path="/books/edit/:id" element={<BookForm />} />
            <Route path="/magazines/new" element={<MagazineForm />} />
            <Route path="/magazines/edit/:id" element={<MagazineForm />} />
          </Route>
          <Route path="/issue/:itemId" element={<IssuePage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;