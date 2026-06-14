import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Select, Button, Space, message, Card, Typography, DatePicker, Upload } from 'antd';
import api from '../services/api';
import { PlusOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { Option } = Select;
const { TextArea } = Input;

const BugForm = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [form] = Form.useForm();

  React.useEffect(() => {
    fetchProjects();
    fetchUsers();
  }, []);

  const fetchProjects = async () => {
    try {
      const response = await api.get('/api/projects');
      setProjects(response.data || []);
    } catch (error) {
      message.error('Failed to fetch projects');
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await api.get('/api/public/users');
      setUsers(response.data || []);
    } catch (error) {
      message.error('Failed to fetch users');
    }
  };

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // Debug: Log form data
      // console.log('Form values being submitted:', values);
      // console.log('Form values type:', typeof values);
      
      const bugData = {
        ...values
      };
      
      // console.log('Bug data to be sent:', bugData);
      
      await api.post('/api/bugs/public', bugData);
      message.success('Bug created successfully!');
      navigate('/bugs');
    } catch (error) {
      console.error('Bug creation error:', error);
      console.error('Error response:', error.response?.data);
      message.error('Failed to create bug');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Title level={2} style={{ marginBottom: '24px' }}>
          <PlusOutlined /> Create New Bug
        </Title>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          style={{ maxWidth: '600px' }}
        >
          <Form.Item
            label="Title"
            name="title"
            rules={[{ required: true, message: 'Please enter bug title' }]}
          >
            <Input placeholder="Enter bug title" />
          </Form.Item>

          <Form.Item
            label="Description"
            name="description"
            rules={[{ required: true, message: 'Please enter bug description' }]}
          >
            <TextArea rows={4} placeholder="Describe the bug in detail" />
          </Form.Item>

          <Form.Item
            label="Project"
            name="projectId"
            rules={[{ required: true, message: 'Please select a project' }]}
          >
            <Select placeholder="Select a project">
              {projects.map(project => (
                <Option key={project.id} value={project.id}>
                  {project.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Priority"
            name="priority"
            rules={[{ required: true, message: 'Please select priority' }]}
          >
            <Select placeholder="Select priority">
              <Option value="CRITICAL">Critical</Option>
              <Option value="HIGH">High</Option>
              <Option value="MEDIUM">Medium</Option>
              <Option value="LOW">Low</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="Assigned To"
            name="assignedTo"
          >
            <Select placeholder="Assign to user (optional)">
              {users.map(user => (
                <Option key={user.id} value={user.id}>
                  {user.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Reporter"
            name="reporterId"
            rules={[{ required: true, message: 'Please select reporter' }]}
          >
            <Select placeholder="Select reporter">
              {users.map(user => (
                <Option key={user.id} value={user.id}>
                  {user.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Attachments"
            name="attachments"
          >
            <Upload
              beforeUpload={() => false}
              multiple
              fileList={[]}
            >
              <Button icon={<PlusOutlined />}>Upload Files</Button>
            </Upload>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                Create Bug
              </Button>
              <Button onClick={() => navigate('/bugs')}>
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default BugForm;
