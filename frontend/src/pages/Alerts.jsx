import { useState, useEffect } from 'react'
import { Tabs, Table, Tag, Button, message } from 'antd'
import { getAlerts, confirmAlert, getRealtimeAlerts } from '../api'
import AlertConfirmModal from '../components/AlertConfirmModal'
import { formatDateTime } from '../utils/formatters'

const { TabPane } = Tabs

export default function Alerts() {
  const [offlineAlerts, setOfflineAlerts] = useState([])
  const [realtimeAlerts, setRealtimeAlerts] = useState([])
  const [loading, setLoading] = useState({ offline: false, realtime: false })
  const [confirmModalVisible, setConfirmModalVisible] = useState(false)
  const [currentAlert, setCurrentAlert] = useState(null)

  const fetchOfflineAlerts = async () => {
    setLoading(prev => ({ ...prev, offline: true }))
    try {
      const res = await getAlerts()
      setOfflineAlerts(res.data)
    } catch (error) {
      message.error('获取离线告警失败')
    } finally {
      setLoading(prev => ({ ...prev, offline: false }))
    }
  }

  const fetchRealtimeAlerts = async () => {
    setLoading(prev => ({ ...prev, realtime: true }))
    try {
      const res = await getRealtimeAlerts()
      setRealtimeAlerts(res.data)
    } catch (error) {
      message.error('获取实时告警失败')
    } finally {
      setLoading(prev => ({ ...prev, realtime: false }))
    }
  }

  useEffect(() => {
    fetchOfflineAlerts()
    fetchRealtimeAlerts()
  }, [])

  const handleConfirm = (record) => {
    setCurrentAlert(record)
    setConfirmModalVisible(true)
  }

  const handleConfirmSubmit = async (confirmBy) => {
    if (!confirmBy) {
      message.warning('请输入确认人')
      return
    }
    try {
      await confirmAlert(currentAlert.id, confirmBy)
      message.success('确认成功')
      setConfirmModalVisible(false)
      fetchOfflineAlerts()
    } catch (error) {
      message.error('确认失败')
    }
  }

  const offlineColumns = [
    { title: 'ID', dataIndex: 'id' },
    { title: '规则ID', dataIndex: 'ruleId' },
    { title: '触发时间', dataIndex: 'triggerTime', render: formatDateTime },
    { title: '异常值', dataIndex: 'abnormalValue' },
    { title: '阈值', dataIndex: 'thresholdValue' },
    { title: '通知方式', dataIndex: 'alertMethod' },
    { 
      title: '确认状态', 
      dataIndex: 'confirmed',
      render: (val) => <Tag color={val === 1 ? 'green' : 'orange'}>{val === 1 ? '已确认' : '未确认'}</Tag>
    },
    { title: '确认人', dataIndex: 'confirmedBy', render: (val) => val || '-' },
    { title: '确认时间', dataIndex: 'confirmedTime', render: formatDateTime },
    {
      title: '操作',
      render: (_, record) => (
        record.confirmed === 0 ? (
          <Button size="small" onClick={() => handleConfirm(record)}>确认</Button>
        ) : null
      )
    }
  ]

  const realtimeColumns = [
    { title: 'RowKey', dataIndex: 'rowKey' },
    { title: '规则ID', dataIndex: 'ruleId' },
    { title: '异常率', dataIndex: 'rate', render: val => (parseFloat(val)*100).toFixed(2)+'%' },
    { title: '阈值', dataIndex: 'threshold', render: val => (parseFloat(val)*100).toFixed(2)+'%' },
    { title: '总数', dataIndex: 'total' },
    { title: '异常数', dataIndex: 'anomaly' },
    { title: '窗口结束时间', dataIndex: 'windowEnd', render: val => new Date(parseInt(val)).toLocaleString() },
    { title: '时间戳', dataIndex: 'timestamp', render: val => new Date(parseInt(val)).toLocaleString() },
  ]

  return (
    <div>
      <Tabs defaultActiveKey="offline">
        <TabPane tab="离线告警" key="offline">
          <Table 
            rowKey="id" 
            columns={offlineColumns} 
            dataSource={offlineAlerts} 
            loading={loading.offline}
          />
        </TabPane>
        <TabPane tab="实时告警" key="realtime">
          <Table 
            rowKey="rowKey" 
            columns={realtimeColumns} 
            dataSource={realtimeAlerts} 
            loading={loading.realtime}
          />
        </TabPane>
      </Tabs>

      <AlertConfirmModal
        visible={confirmModalVisible}
        onCancel={() => setConfirmModalVisible(false)}
        onConfirm={handleConfirmSubmit}
      />
    </div>
  )
}
