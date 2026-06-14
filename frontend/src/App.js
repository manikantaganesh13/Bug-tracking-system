import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout, Spin } from 'antd';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import BugList from './components/BugList';
import BugDetail from './components/BugDetail';
import BugForm from './components/BugForm';
import ProjectList from './components/ProjectList';
import UserList from './components/UserList';
import Profile from './components/Profile';
import AppHeader from './components/AppHeader';
import AppSider from './components/AppSider';
import { AuthProvider, useAuth } from './contexts/AuthContext';

const { Content } = Layout;

function AppRoutes() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!user) {
    return (
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <AppSider />
      <Layout>
        <AppHeader />
        <Content style={{ margin: '24px 16px 0', overflow: 'initial' }}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/bugs" element={<BugList />} />
            <Route path="/bugs/:id" element={<BugDetail />} />
            <Route path="/bugs/new" element={<BugForm />} />
            <Route path="/projects" element={<ProjectList />} />
            <Route path="/profile" element={<Profile />} />
            {user?.role === 'ADMIN' && <Route path="/users" element={<UserList />} />}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;
