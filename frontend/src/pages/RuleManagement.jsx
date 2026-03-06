import { useState, useEffect } from 'react'
import { Table, Button, Space, Modal, Form, Input, Select, Switch, message, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { getRules, createRule, updateRule, deleteRule, triggerRule } from '../api'
import { parseJsonField, stringifyJson } from '../utils/formatters'

const { Option } = Select
const { TextArea } = Input

export default function RuleManagement() {
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRule, setEditingRule] = useState(null)
  const [form] = Form.useForm()

  const fetchRules = async () => {
    setLoading(true)
    try {
      const res = await getRules()
      setRules(res.data)
    } catch (error) {
      message.error('获取规则列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRules()
  }, [])

  const handleAdd = () => {
    setEditingRule(null)
    form.resetFields()
    form.setFieldsValue({
      status: true,           
      sourceType: 'HDFS',
      sourceConfig: JSON.stringify({ path: "/data", format: "csv" }, null, 2),
      ruleParams: JSON.stringify({ column: "age", threshold: 0.05 }, null, 2),
      scheduleConfig: JSON.stringify({ cron: "0 0 2 * * ?" }, null, 2),
      windowConfig: '',    
    })
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingRule(record)
    form.setFieldsValue({
      ...record,
      status: record.status === 1, 
      sourceConfig: stringifyJson(parseJsonField(record.sourceConfig)),
      ruleParams: stringifyJson(parseJsonField(record.ruleParams)),
      scheduleConfig: stringifyJson(parseJsonField(record.scheduleConfig)),
      windowConfig: record.windowConfig ? stringifyJson(parseJsonField(record.windowConfig)) : '',
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该规则吗？',
      onOk: async () => {
        try {
          await deleteRule(id)
          message.success('删除成功')
          fetchRules()
        } catch (error) {
          message.error('删除失败')
        }
      }
    })
  }

  const handleTrigger = async (id) => {
    try {
      await triggerRule(id)
      message.success('触发成功')
    } catch (error) {
      message.error('触发失败')
    }
  }

  const handleModalOk = () => {
    form.validateFields().then(async values => {
      const payload = {
        ...values,
        status: values.status ? 1 : 0, 
        sourceConfig: JSON.stringify(parseJsonField(values.sourceConfig)),
        ruleParams: JSON.stringify(parseJsonField(values.ruleParams)),
        scheduleConfig: JSON.stringify(parseJsonField(values.scheduleConfig)),
        windowConfig: values.windowConfig ? JSON.stringify(parseJsonField(values.windowConfig)) : null,
      }
      try {
        if (editingRule) {
          await updateRule(editingRule.id, payload)
          message.success('更新成功')
        } else {
          await createRule(payload)
          message.success('创建成功')
        }
        setModalVisible(false)
        fetchRules()
      } catch (error) {
        message.error('操作失败')
      }
    })
  }

  const columns = [
    { title: 'ID', dataIndex: 'id' },
    { title: '规则名称', dataIndex: 'ruleName' },
    { title: '数据源类型', dataIndex: 'sourceType' },
    { title: '规则类型', dataIndex: 'ruleType' },
    { 
      title: '状态', 
      dataIndex: 'status',
      render: (val) => <Tag color={val === 1 ? 'green' : 'red'}>{val === 1 ? '启用' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createdAt' },
    {
      title: '操作',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)}>编辑</Button>
          <Button icon={<DeleteOutlined />} size="small" danger onClick={() => handleDelete(record.id)}>删除</Button>
          <Button icon={<PlayCircleOutlined />} size="small" onClick={() => handleTrigger(record.id)}>触发</Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} style={{ marginBottom: 16 }}>
        新增规则
      </Button>
      <Table 
        rowKey="id" 
        columns={columns} 
        dataSource={rules} 
        loading={loading}
      />
      <Modal
        title={editingRule ? '编辑规则' : '新增规则'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={800}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="ruleName" label="规则名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="sourceType" label="数据源类型" rules={[{ required: true }]}>
            <Select>
              <Option value="HDFS">HDFS</Option>
              <Option value="KAFKA">KAFKA</Option>
            </Select>
          </Form.Item>
          <Form.Item name="sourceConfig" label="数据源配置(JSON)" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder='例如：{"path":"/data","format":"csv"}' />
          </Form.Item>
          <Form.Item name="ruleType" label="规则类型" rules={[{ required: true }]}>
            <Select>
              <Option value="NULL_CHECK">NULL_CHECK</Option>
              <Option value="UNIQUE_CHECK">UNIQUE_CHECK</Option>
              <Option value="RANGE_CHECK">RANGE_CHECK</Option>
              <Option value="SQL_EXPRESSION">SQL_EXPRESSION</Option>
            </Select>
          </Form.Item>
          <Form.Item name="ruleParams" label="规则参数(JSON)" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder='例如：{"column":"age","threshold":0.05}' />
          </Form.Item>
          <Form.Item name="scheduleConfig" label="调度配置(JSON)" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder='必须包含cron字段，例如：{"cron":"0 0 2 * * ?"}' />
          </Form.Item>
          <Form.Item name="windowConfig" label="窗口配置(JSON)">
            <TextArea rows={3} placeholder='可选，实时规则时使用' />
          </Form.Item>
          <Form.Item name="status" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
