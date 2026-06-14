import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Select, message, Row, Col } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined, TeamOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const { Title } = Typography;
const { Option } = Select;

const Register = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      console.log('Registration data:', values);
      const response = await api.post('/api/auth/register', values);
      console.log('Registration response:', response.data);
      message.success('Registration successful! Please login.');
      navigate('/login');
    } catch (error) {
      console.error('Registration error:', error);
      console.error('Error response:', error.response?.data);
      message.error(error.response?.data || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-form">
      <Card className="register-card">
        <div className="register-header">
          <TeamOutlined className="register-icon" />
          <Title level={2} className="register-title">
            Create Account
          </Title>
          <p className="register-subtitle">
            Join our Bug Tracking System and start managing your projects efficiently
          </p>
        </div>
        
        <Form
          name="register"
          onFinish={onFinish}
          layout="vertical"
          size="large"
          className="register-form-inner"
        >
          <Row gutter={16}>
            <Col span={24}>
              <Form.Item
                name="name"
                label="Full Name"
                rules={[
                  { required: true, message: 'Please input your full name!' },
                  { min: 2, message: 'Name must be at least 2 characters!' },
                  { max: 50, message: 'Name cannot exceed 50 characters!' }
                ]}
              >
                <Input 
                  prefix={<UserOutlined />} 
                  placeholder="Enter your full name" 
                  className="register-input"
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={24}>
              <Form.Item
                name="email"
                label="Email Address"
                rules={[
                  { required: true, message: 'Please input your email!' },
                  { type: 'email', message: 'Please enter a valid email!' }
                ]}
              >
                <Input 
                  prefix={<MailOutlined />} 
                  placeholder="Enter your email address" 
                  className="register-input"
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={24}>
              <Form.Item
                name="password"
                label="Password"
                rules={[
                  { required: true, message: 'Please input your password!' },
                  { min: 6, message: 'Password must be at least 6 characters!' },
                  { max: 20, message: 'Password cannot exceed 20 characters!' }
                ]}
              >
                <Input.Password 
                  prefix={<LockOutlined />} 
                  placeholder="Create a strong password" 
                  className="register-input"
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={24}>
              <Form.Item
                name="role"
                label="Select Your Role"
                rules={[{ required: true, message: 'Please select your role!' }]}
              >
                <Select 
                  placeholder="Choose your role" 
                  className="register-select"
                  size="large"
                >
                  <Option value="TESTER">
                    <TeamOutlined /> Tester - Report and track bugs
                  </Option>
                  <Option value="DEVELOPER">
                    <TeamOutlined /> Developer - Fix assigned bugs
                  </Option>
                  <Option value="ADMIN">
                    <TeamOutlined /> Admin - Manage system
                  </Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item className="register-button-container">
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading} 
              className="register-button"
              size="large"
              block
            >
              Create Account
            </Button>
          </Form.Item>

          <div className="register-login-link">
            Already have an account? 
            <a href="/login" className="register-link"> Sign In</a>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Register;
