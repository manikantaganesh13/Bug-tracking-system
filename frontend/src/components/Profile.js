import React from 'react';
import { Card, Descriptions, Avatar, Tag, Typography, Space, Divider } from 'antd';
import { UserOutlined, MailOutlined, CalendarOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';

const { Title } = Typography;

const Profile = () => {
  const { user } = useAuth();

  if (!user) {
    return <div>Loading profile...</div>;
  }

  const getRoleColor = (role) => {
    const colors = {
      ADMIN: 'red',
      DEVELOPER: 'blue',
      TESTER: 'green',
    };
    return colors[role] || 'default';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* Header Section */}
          <div style={{ textAlign: 'center' }}>
            <Avatar 
              size={80} 
              icon={<UserOutlined />} 
              style={{ marginBottom: 16 }}
            />
            <Title level={3}>{user.name}</Title>
            <Tag color={getRoleColor(user.role)} style={{ marginBottom: 8 }}>
              {user.role}
            </Tag>
          </div>

          <Divider />

          {/* User Details */}
          <Descriptions title="User Information" column={1} bordered>
            <Descriptions.Item 
              label="Name" 
              icon={<UserOutlined />}
            >
              {user.name}
            </Descriptions.Item>
            
            <Descriptions.Item 
              label="Email" 
              icon={<MailOutlined />}
            >
              {user.email}
            </Descriptions.Item>
            
            <Descriptions.Item 
              label="Role" 
              icon={<UserOutlined />}
            >
              <Tag color={getRoleColor(user.role)}>
                {user.role}
              </Tag>
            </Descriptions.Item>
            
            {/* <Descriptions.Item 
              label="Member Since" 
              icon={<CalendarOutlined />}
            >
              {formatDate(user.createdDate)}
            </Descriptions.Item> */}
            
            {/* <Descriptions.Item label="User ID">
              {user.id}
            </Descriptions.Item> */}
          </Descriptions>

          {/* Role-specific information */}
          <Descriptions title="Role Information" column={1} bordered>
            {user.role === 'ADMIN' && (
              <>
                <Descriptions.Item label="Permissions">
                  <Tag color="red">Full System Access</Tag>
                  <Tag color="orange">User Management</Tag>
                  <Tag color="purple">Project Management</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Can Manage">
                  All users, projects, and bugs in the system
                </Descriptions.Item>
              </>
            )}
            
            {user.role === 'DEVELOPER' && (
              <>
                <Descriptions.Item label="Permissions">
                  <Tag color="blue">View Bugs</Tag>
                  <Tag color="cyan">Update Bug Status</Tag>
                  <Tag color="green">Add Comments</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Can Manage">
                  Bugs assigned to them, add comments, update status
                </Descriptions.Item>
              </>
            )}
            
            {user.role === 'TESTER' && (
              <>
                <Descriptions.Item label="Permissions">
                  <Tag color="green">Create Bugs</Tag>
                  <Tag color="orange">View All Bugs</Tag>
                  <Tag color="blue">Add Comments</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Can Manage">
                  Create new bug reports, view all bugs, add comments
                </Descriptions.Item>
              </>
            )}
          </Descriptions>
        </Space>
      </Card>
    </div>
  );
};

export default Profile;
