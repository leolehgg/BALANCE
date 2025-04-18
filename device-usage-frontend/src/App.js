// src/App.js
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider, useAuth } from './context/AuthContext';

// Páginas
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Dashboard from './pages/dashboard/Dashboard';
import DeviceList from './pages/devices/DeviceList';
import DeviceDetail from './pages/devices/DeviceDetail';
import SessionHistory from './pages/usage/SessionHistory';
import LimitList from './pages/limits/LimitList';
import Layout from './components/layout/Layout_t';

// Ruta protegida por autenticación
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) return <div>Cargando...</div>;
  
  if (!user) {
    return <Navigate to="/login" />;
  }
  
  return children;
};

// Tema personalizado
const theme = createTheme({
  palette: {
    primary: {
      main: '#3f51b5',
    },
    secondary: {
      main: '#f50057',
    },
    background: {
      default: '#f5f5f5',
    },
  },
});

function AppContent() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/devices" element={
          <ProtectedRoute>
            <Layout>
              <DeviceList />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/devices/:id" element={
          <ProtectedRoute>
            <Layout>
              <DeviceDetail />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/history" element={
          <ProtectedRoute>
            <Layout>
              <SessionHistory />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/limits" element={
          <ProtectedRoute>
            <Layout>
              <LimitList />
            </Layout>
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;