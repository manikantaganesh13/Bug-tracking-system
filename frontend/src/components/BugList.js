import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Space, Input, Select, Tag, Modal, message } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const { Search } = Input;
const { Option } = Select;

const BugList = () => {
  const [bugs, setBugs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState(null);
  const [severityFilter, setSeverityFilter] = useState(null);
  const navigate = useNavigate();
  const { user } = useAuth();
  const [tablePagination, setTablePagination] = useState({
    current: 1,
    pageSize: 10,
  });

  useEffect(() => {
    fetchBugs();
  }, [searchText, statusFilter, severityFilter]);

  const fetchBugs = async () => {
    setLoading(true);
    try {
      const params = {};
      if (searchText) params.title = searchText;
      if (statusFilter) params.status = statusFilter;
      if (severityFilter) params.severity = severityFilter;

      const response = await api.get('/api/public/bugs/search', { params });
      setBugs(response.data);
    } catch (error) {
      message.error('Failed to fetch bugs');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (id) => {
    Modal.confirm({
      title: 'Are you sure you want to delete this bug?',
      content: 'This action cannot be undone.',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk: async () => {
        try {
          await api.delete(`/api/bugs/${id}`);
          message.success('Bug deleted successfully');
          fetchBugs();
        } catch (error) {
          message.error('Failed to delete bug');
        }
      },
    });
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

  const columns = [
    // {
    //   title: 'ID',
    //   dataIndex: 'id',
    //   key: 'id',
    //   width: 80,
    // },
    {
      title: "S.No",
      key: "serialNo",
      width: 80,
      render: (_, __, index) =>
        (tablePagination.current - 1) * tablePagination.pageSize + index + 1,
    },
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <Button type="link" onClick={() => navigate(`/bugs/${record.id}`)}>
          {text}
        </Button>
      ),
    },
    {
      title: 'Project',
      dataIndex: 'projectName',
      key: 'project',
    },
    {
      title: 'Severity',
      dataIndex: 'severity',
      key: 'severity',
      render: (severity) => (
        <Tag color={getSeverityColor(severity)}>{severity}</Tag>
      ),
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={getStatusColor(status)}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Assigned To',
      dataIndex: 'assignedToName',
      key: 'assignedTo',
      render: (name) => name || 'Unassigned',
    },
    {
      title: 'Created Date',
      dataIndex: 'createdDate',
      key: 'createdDate',
      render: (date) => new Date(date).toLocaleDateString(),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            size="small"
            onClick={() => navigate(`/bugs/${record.id}`)}
          >
            Edit
          </Button>
          {user?.role === 'ADMIN' && (
            <Button
              icon={<DeleteOutlined />}
              size="small"
              danger
              onClick={() => handleDelete(record.id)}
            >
              Delete
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <Card title="Bug Management">
      <Space style={{ marginBottom: 16 }}>
        <Search
          placeholder="Search bugs..."
          allowClear
          enterButton={<SearchOutlined />}
          style={{ width: 300 }}
          onSearch={setSearchText}
          onChange={(e) => !e.target.value && setSearchText('')}
        />
        <Select
          placeholder="Filter by status"
          allowClear
          style={{ width: 150 }}
          onChange={setStatusFilter}
        >
          <Option value="OPEN">Open</Option>
          <Option value="ASSIGNED">Assigned</Option>
          <Option value="IN_PROGRESS">In Progress</Option>
          <Option value="RESOLVED">Resolved</Option>
          <Option value="CLOSED">Closed</Option>
        </Select>
        <Select
          placeholder="Filter by severity"
          allowClear
          style={{ width: 150 }}
          onChange={setSeverityFilter}
        >
          <Option value="CRITICAL">Critical</Option>
          <Option value="HIGH">High</Option>
          <Option value="MEDIUM">Medium</Option>
          <Option value="LOW">Low</Option>
        </Select>
        {(user?.role === 'TESTER' || user?.role === 'ADMIN') && (
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/bugs/new')}
          >
            Create Bug
          </Button>
        )}
      </Space>

      <Table
        columns={columns}
        dataSource={bugs}
        loading={loading}
        rowKey="id"
        // pagination={{
        //   pageSize: 10,
        //   showSizeChanger: true,
        //   showQuickJumper: true,
        //   showTotal: (total, range) =>
        //     `${range[0]}-${range[1]} of ${total} bugs`,
        // }}
        pagination={{
          ...tablePagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) =>
            `${range[0]}-${range[1]} of ${total} bugs`,
        }}
        onChange={(pagination) => setTablePagination(pagination)}
      />
    </Card>
  );
};

export default BugList;
