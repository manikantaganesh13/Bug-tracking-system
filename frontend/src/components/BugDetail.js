import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Card, 
  Descriptions, 
  Tag, 
  Button, 
  Space, 
  List, 
  Form, 
  Input, 
  Select, 
  message,
  Modal,
  Divider,
  Avatar
} from 'antd';
import { ArrowLeftOutlined, EditOutlined, SaveOutlined } from '@ant-design/icons';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const { Option } = Select;
const { TextArea } = Input;

const BugDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [bug, setBug] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentLoading, setCommentLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [form] = Form.useForm();
  const [users, setUsers] = useState([]);

  useEffect(() => {
    if (id !== 'new') {
      fetchBugDetail();
      fetchComments();
      fetchUsers();
    } else {
      setLoading(false);
    }
  }, [id]);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/api/public/developers');
      setUsers(response.data || []);
    } catch (error) {
      message.error('Failed to fetch developers');
    }
  };

  const fetchBugDetail = async () => {
    try {
      const response = await api.get(`/api/public/bugs/${id}`);
      setBug(response.data);
      
      // Set form values including assignedToId
      const formValues = {
        ...response.data,
        assignedToId: response.data.assignedToId // Set the correct field name
      };
      form.setFieldsValue(formValues);
    } catch (error) {
      message.error('Failed to fetch bug details');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    try {
      const response = await api.get(`/api/public/comments/bug/${id}`);
      setComments(response.data);
    } catch (error) {
      message.error('Failed to fetch comments');
    }
  };

  const handleCommentSubmit = async (values) => {
    setCommentLoading(true);
    try {
      await api.post('/api/public/comments', {
        commentText: values.comment,
        bugId: parseInt(id),
        userId: user?.id,
      });
      message.success('Comment added successfully');
      form.resetFields(['comment']);
      fetchComments();
    } catch (error) {
      message.error('Failed to add comment');
    } finally {
      setCommentLoading(false);
    }
  };

  const handleUpdateBug = async (values) => {
    console.log('Form values being submitted:', values); // Debug log
    try {
      await api.put(`/api/public/bugs/${id}`, values);
      message.success('Bug updated successfully');
      setEditMode(false);
      fetchBugDetail();
    } catch (error) {
      message.error('Failed to update bug');
    }
  };

  const handleStatusUpdate = async (status) => {
    try {
      await api.put(`/api/public/bugs/${id}/status`, { status });
      message.success('Status updated successfully');
      fetchBugDetail();
    } catch (error) {
      message.error('Failed to update status');
    }
  };

  const getSeverityColor = (severity) => {
    const colors = {
      CRITICAL: 'purple',
      HIGH: 'red',
      MEDIUM: 'orange',
      LOW: 'green',
    };
    return colors[severity] || 'default';
  };

  const getStatusColor = (status) => {
    const colors = {
      OPEN: 'blue',
      ASSIGNED: 'cyan',
      IN_PROGRESS: 'orange',
      RESOLVED: 'green',
      CLOSED: 'default',
      REOPENED: 'red',
      REJECTED: 'gray',
    };
    return colors[status] || 'default';
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (id === 'new') {
    return <div>Create new bug form would go here</div>;
  }

  return (
    <div>
      <Button 
        icon={<ArrowLeftOutlined />} 
        onClick={() => navigate('/bugs')}
        style={{ marginBottom: 16 }}
      >
        Back to Bugs
      </Button>

      <Card
        title={`Bug: ${bug?.title}`}
        extra={
          <Space>
            {(user?.role === 'DEVELOPER' || user?.role === 'ADMIN') && (
              <Select
                placeholder="Update Status"
                style={{ width: 150 }}
                onChange={handleStatusUpdate}
                value={bug?.status}
              >
                <Option value="OPEN">Open</Option>
                <Option value="ASSIGNED">Assigned</Option>
                <Option value="IN_PROGRESS">In Progress</Option>
                <Option value="RESOLVED">Resolved</Option>
                <Option value="CLOSED">Closed</Option>
                <Option value="REOPENED">Reopened</Option>
                <Option value="REJECTED">Rejected</Option>
              </Select>
            )}
            <Button 
              icon={editMode ? <SaveOutlined /> : <EditOutlined />}
              onClick={() => {
                if (editMode) {
                  form.submit();
                } else {
                  setEditMode(true);
                }
              }}
            >
              {editMode ? 'Save' : 'Edit'}
            </Button>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleUpdateBug}
        >
          <Descriptions bordered column={2}>
            <Descriptions.Item label="ID">{bug?.id}</Descriptions.Item>
            <Descriptions.Item label="Project">{bug?.projectName}</Descriptions.Item>
            <Descriptions.Item label="Severity">
              {editMode ? (
                <Form.Item name="severity" noStyle>
                  <Select>
                    <Option value="CRITICAL">Critical</Option>
                    <Option value="HIGH">High</Option>
                    <Option value="MEDIUM">Medium</Option>
                    <Option value="LOW">Low</Option>
                  </Select>
                </Form.Item>
              ) : (
                <Tag color={getSeverityColor(bug?.severity)}>{bug?.severity}</Tag>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="Priority">
              {editMode ? (
                <Form.Item name="priority" noStyle>
                  <Select>
                    <Option value="URGENT">Urgent</Option>
                    <Option value="HIGH">High</Option>
                    <Option value="MEDIUM">Medium</Option>
                    <Option value="LOW">Low</Option>
                  </Select>
                </Form.Item>
              ) : (
                bug?.priority
              )}
            </Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={getStatusColor(bug?.status)}>
                {bug?.status?.replace('_', ' ')}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Assigned To">
              {editMode && (user?.role === 'ADMIN' || user?.role === 'TESTER') ? (
                <Form.Item name="assignedToId" noStyle>
                  <Select placeholder="Select user to assign" allowClear>
                    {users.map(u => (
                      <Option key={u.id} value={u.id}>{u.name}</Option>
                    ))}
                  </Select>
                </Form.Item>
              ) : (
                bug?.assignedToName || 'Unassigned'
              )}
            </Descriptions.Item>
            <Descriptions.Item label="Created By">
              {bug?.createdByName}
            </Descriptions.Item>
            <Descriptions.Item label="Created Date">
              {new Date(bug?.createdDate).toLocaleDateString()}
            </Descriptions.Item>
            <Descriptions.Item label="Description" span={2}>
              {editMode ? (
                <Form.Item name="description" noStyle>
                  <TextArea rows={4} />
                </Form.Item>
              ) : (
                bug?.description
              )}
            </Descriptions.Item>
          </Descriptions>
        </Form>
      </Card>

      <Card title="Comments" style={{ marginTop: 16 }}>
        <Form onFinish={handleCommentSubmit}>
          <Form.Item name="comment" rules={[{ required: true, message: 'Please enter a comment' }]}>
            <TextArea rows={3} placeholder="Add a comment..." />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={commentLoading}>
              Add Comment
            </Button>
          </Form.Item>
        </Form>

        <Divider />

        <List
          dataSource={comments}
          renderItem={(comment) => (
            <List.Item>
              <List.Item.Meta
                avatar={<Avatar>{comment.userName?.charAt(0)?.toUpperCase()}</Avatar>}
                title={
                  <div>
                    {comment.userName}
                    {comment.userRole && (
                      <Tag 
                        size="small" 
                        style={{ marginLeft: 8 }}
                        color={comment.userRole === 'ADMIN' ? 'red' : 
                               comment.userRole === 'DEVELOPER' ? 'blue' : 'green'}
                      >
                        {comment.userRole}
                      </Tag>
                    )}
                  </div>
                }
                description={
                  <div>
                    <p>{comment.commentText}</p>
                    <div style={{ color: '#8c8c8c', fontSize: '12px' }}>
                      {new Date(comment.createdDate).toLocaleString()}
                    </div>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Card>
    </div>
  );
};

export default BugDetail;
