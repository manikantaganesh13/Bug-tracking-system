import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography } from 'antd';
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';

const { Title } = Typography;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const onFinish = async (values) => {
    setLoading(true);
    const success = await login(values.email, values.password);
    setLoading(false);
  };

  return (
    <div className="login-form">
      <Card className="login-card">
        <div className="login-header">
          <LoginOutlined className="login-icon" />
          <Title level={2} className="login-title">
            Bug Tracking System
          </Title>
          <p className="login-subtitle">
            Sign in to manage your projects and track bugs
          </p>
        </div>
        
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
          className="login-form-inner"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Please input your email!' },
              { type: 'email', message: 'Please enter a valid email!' }
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="Email address" 
              className="login-input"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input your password!' }]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="Password" 
              className="login-input"
            />
          </Form.Item>

          <Form.Item className="login-button-container">
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading} 
              className="login-button"
              block
            >
              Sign In
            </Button>
          </Form.Item>

          <div className="login-register-link">
            Don't have an account? 
            <a href="/register" className="login-link"> Register here</a>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
