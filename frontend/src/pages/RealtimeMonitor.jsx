import { useState, useEffect } from 'react'
import { Row, Col, Card, Table, Tag, message } from 'antd'
import { getRealtimeLatest, getRealtimeRules } from '../api'
import { useInterval } from '../hooks/useInterval'
import RealtimeTrendChart from '../components/RealtimeTrendChart'
import { formatRate } from '../utils/formatters'

export default function RealtimeMonitor() {
  const [latestMetrics, setLatestMetrics] = useState({})
  const [historicalData, setHistoricalData] = useState({}) // { ruleId: [{ rate, windowEnd }] }
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(false)

  // 获取启用的实时规则列表（用于显示规则名称）
  const fetchRules = async () => {
    try {
      const res = await getRealtimeRules()
      setRules(res.data)
    } catch (error) {
      message.error('获取规则列表失败')
    }
  }

  // 获取最新指标，并更新历史数据
  const fetchLatest = async () => {
    try {
      const res = await getRealtimeLatest()
      const newMetrics = res.data
      setLatestMetrics(newMetrics)

      // 更新历史数据：保留每个规则最近20个点
      setHistoricalData(prev => {
        const updated = { ...prev }
        Object.keys(newMetrics).forEach(ruleId => {
          const metric = newMetrics[ruleId]
          const point = { rate: metric.rate, windowEnd: metric.windowEnd }
          const ruleHistory = prev[ruleId] || []
          // 避免重复添加同一窗口
          if (!ruleHistory.some(p => p.windowEnd === point.windowEnd)) {
            const newHistory = [...ruleHistory, point].slice(-20)
            updated[ruleId] = newHistory
          } else {
            updated[ruleId] = ruleHistory
          }
        })
        return updated
      })
    } catch (error) {
      message.error('获取实时指标失败')
    }
  }

  useEffect(() => {
    fetchRules()
    fetchLatest()
  }, [])

  // 每5秒轮询
  useInterval(() => {
    fetchLatest()
  }, 5000)

  const columns = [
    { title: '规则ID', dataIndex: 'ruleId', key: 'ruleId' },
    { 
      title: '规则名称', 
      key: 'ruleName',
      render: (_, record) => {
        const rule = rules.find(r => r.id === parseInt(record.ruleId))
        return rule ? rule.ruleName : '-'
      }
    },
    { 
      title: '最新异常率', 
      dataIndex: 'rate',
      render: (rate) => {
        const color = rate > 0.3 ? 'red' : rate > 0.1 ? 'orange' : 'green'
        return <Tag color={color}>{formatRate(rate)}</Tag>
      }
    },
    { title: '总数', dataIndex: 'total' },
    { title: '异常数', dataIndex: 'anomaly' },
    { 
      title: '窗口结束时间', 
      dataIndex: 'windowEnd',
      render: (val) => new Date(val).toLocaleString()
    },
  ]

  // 将latestMetrics转换为表格数据
  const tableData = Object.entries(latestMetrics).map(([ruleId, metric]) => ({
    ruleId,
    ...metric,
  }))

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="实时异常率趋势">
            <RealtimeTrendChart data={historicalData} />
          </Card>
        </Col>
        <Col span={24}>
          <Card title="当前异常率">
            <Table 
              rowKey="ruleId" 
              columns={columns} 
              dataSource={tableData} 
              loading={loading}
              pagination={false}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
