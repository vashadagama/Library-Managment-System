import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import LoginScreen from './screens/LoginScreen';
import Dashboard from './screens/Dashboard';
import CatalogScreen from './screens/CatalogScreen';
import BookDetailScreen from './screens/BookDetailScreen';
import MagazineDetailScreen from './screens/MagazineDetailScreen';
import AuthorsScreen from './screens/AuthorsScreen';
import ReadersScreen from './screens/ReadersScreen';
import LoansScreen from './screens/LoansScreen';
import FinesScreen from './screens/FinesScreen';
import StatisticsScreen from './screens/StatisticsScreen';
import IssuePage from './screens/IssuePage';
import BookForm from './components/BookForm';
import MagazineForm from './components/MagazineForm';
import NotFound from './components/NotFound';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const STAFF_ROLES = ['ROLE_LIBRARIAN', 'ROLE_ADMIN'];

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
            <Route
              path="/authors"
              element={<PrivateRoute roles={STAFF_ROLES}><AuthorsScreen /></PrivateRoute>}
            />
            <Route
              path="/readers"
              element={<PrivateRoute roles={STAFF_ROLES}><ReadersScreen /></PrivateRoute>}
            />
            <Route
              path="/loans"
              element={<PrivateRoute roles={STAFF_ROLES}><LoansScreen /></PrivateRoute>}
            />
            <Route
              path="/fines"
              element={<PrivateRoute roles={STAFF_ROLES}><FinesScreen /></PrivateRoute>}
            />
            <Route
              path="/statistics"
              element={<PrivateRoute roles={STAFF_ROLES}><StatisticsScreen /></PrivateRoute>}
            />
            <Route
              path="/issue/:itemId"
              element={<PrivateRoute roles={STAFF_ROLES}><IssuePage /></PrivateRoute>}
            />
            <Route
              path="/books/new"
              element={<PrivateRoute roles={STAFF_ROLES}><BookForm /></PrivateRoute>}
            />
            <Route
              path="/books/edit/:id"
              element={<PrivateRoute roles={STAFF_ROLES}><BookForm /></PrivateRoute>}
            />
            <Route
              path="/magazines/new"
              element={<PrivateRoute roles={STAFF_ROLES}><MagazineForm /></PrivateRoute>}
            />
            <Route
              path="/magazines/edit/:id"
              element={<PrivateRoute roles={STAFF_ROLES}><MagazineForm /></PrivateRoute>}
            />
          </Route>

          <Route path="*" element={<NotFound />} />
        </Routes>
        <ToastContainer
          position="top-right"
          autoClose={3500}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          pauseOnFocusLoss={false}
          draggable
          pauseOnHover
          theme="light"
          limit={4}
        />
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
