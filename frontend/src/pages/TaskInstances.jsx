import { useState, useEffect } from 'react'
import { Table, Tag, Input, Modal, Descriptions, message } from 'antd'
import { getTasks } from '../api'
import { formatDateTime, parseJsonField } from '../utils/formatters'

const { Search } = Input

export default function TaskInstances() {
  const [tasks, setTasks] = useState([])
  const [filteredTasks, setFilteredTasks] = useState([])
  const [loading, setLoading] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [currentTask, setCurrentTask] = useState(null)

  const fetchTasks = async () => {
    setLoading(true)
    try {
      const res = await getTasks()
      setTasks(res.data)
      setFilteredTasks(res.data)
    } catch (error) {
      message.error('获取任务列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const handleSearch = (value) => {
    if (!value) {
      setFilteredTasks(tasks)
    } else {
      const filtered = tasks.filter(t => t.ruleId.toString().includes(value))
      setFilteredTasks(filtered)
    }
  }

  const showDetail = (record) => {
    setCurrentTask(record)
    setDetailVisible(true)
  }

  const columns = [
    { title: '任务ID', dataIndex: 'id' },
    { title: '规则ID', dataIndex: 'ruleId' },
    { title: '计划时间', dataIndex: 'scheduledTime', render: formatDateTime },
    { title: '开始时间', dataIndex: 'startTime', render: formatDateTime },
    { title: '结束时间', dataIndex: 'endTime', render: formatDateTime },
    { 
      title: '状态', 
      dataIndex: 'status',
      render: (val) => {
        const color = val === 'SUCCESS' ? 'green' : val === 'FAILED' ? 'red' : 'blue'
        return <Tag color={color}>{val}</Tag>
      }
    },
    {
      title: '操作',
      render: (_, record) => (
        <a onClick={() => showDetail(record)}>详情</a>
      )
    }
  ]

  return (
    <div>
      <Search 
        placeholder="输入规则ID筛选" 
        onSearch={handleSearch} 
        style={{ marginBottom: 16, width: 300 }} 
        allowClear
      />
      <Table 
        rowKey="id" 
        columns={columns} 
        dataSource={filteredTasks} 
        loading={loading}
      />
      <Modal
        title="任务详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
      >
        {currentTask && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="任务ID">{currentTask.id}</Descriptions.Item>
            <Descriptions.Item label="规则ID">{currentTask.ruleId}</Descriptions.Item>
            <Descriptions.Item label="计划时间">{formatDateTime(currentTask.scheduledTime)}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{formatDateTime(currentTask.startTime)}</Descriptions.Item>
            <Descriptions.Item label="结束时间">{formatDateTime(currentTask.endTime)}</Descriptions.Item>
            <Descriptions.Item label="状态">{currentTask.status}</Descriptions.Item>
            <Descriptions.Item label="结果摘要">
              <pre>{JSON.stringify(parseJsonField(currentTask.resultSummary), null, 2)}</pre>
            </Descriptions.Item>
            <Descriptions.Item label="错误信息">{currentTask.errorMsg || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  )
}
