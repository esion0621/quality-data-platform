import { useEffect, useState } from 'react'
import { Row, Col, Card, message } from 'antd'
import { getTrend, getPassRates } from '../api'
import TrendChart from '../components/TrendChart'
import PassRateChart from '../components/PassRateChart'

export default function Dashboard() {
  const [trendData, setTrendData] = useState([])
  const [passRates, setPassRates] = useState([])
  const [loading, setLoading] = useState(false)

  const fetchData = async () => {
    setLoading(true)
    try {
      const end = new Date().toISOString().split('T')[0]
      const start = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      const [trendRes, passRes] = await Promise.all([
        getTrend(start, end),
        getPassRates()
      ])
      setTrendData(trendRes.data)
      setPassRates(passRes.data)
    } catch (error) {
      message.error('加载数据失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card loading={loading}>
            <TrendChart data={trendData} />
          </Card>
        </Col>
        <Col span={24}>
          <Card loading={loading}>
            <PassRateChart data={passRates} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
