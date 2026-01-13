import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Context
import { AuthProvider } from './context/AuthContext';

// Components
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import NotificationListener from './components/NotificationListener';

// Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import SlotList from './pages/common/SlotList';
import MyAppointments from './pages/customer/MyAppointments';
import ProviderDashboard from './pages/provider/ProviderDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <AuthProvider>
          <Navbar />
          <div style={{ padding: '20px' }}>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<SlotList />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Protected Routes */}

              {/* Customer */}
              <Route element={<ProtectedRoute roles={['ROLE_CUSTOMER']} />}>
                <Route path="/my-appointments" element={<MyAppointments />} />
              </Route>

              {/* Provider */}
              <Route element={<ProtectedRoute roles={['ROLE_PROVIDER']} />}>
                <Route path="/provider/dashboard" element={<ProviderDashboard />} />
              </Route>

              {/* Admin */}
              <Route element={<ProtectedRoute roles={['ROLE_ADMIN']} />}>
                <Route path="/admin/dashboard" element={<AdminDashboard />} />
              </Route>

            </Routes>
          </div>
          <NotificationListener />
          <ToastContainer />
        </AuthProvider>
      </Router>
    </ThemeProvider >
  );
}

export default App;
