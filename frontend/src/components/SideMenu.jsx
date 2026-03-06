import { Layout, Menu } from 'antd'
import { 
  DashboardOutlined, 
  ApiOutlined, 
  ScheduleOutlined, 
  AlertOutlined, 
  LineChartOutlined 
} from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'

const { Sider } = Layout

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '数据看板' },
  { key: '/rules', icon: <ApiOutlined />, label: '规则管理' },
  { key: '/tasks', icon: <ScheduleOutlined />, label: '任务实例' },
  { key: '/alerts', icon: <AlertOutlined />, label: '告警记录' },
  { key: '/realtime', icon: <LineChartOutlined />, label: '实时监控' },
]

export default function SideMenu() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <Sider>
      <div style={{ height: 32, margin: 16, background: 'rgba(255,255,255,0.2)' }} />
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  )
}
