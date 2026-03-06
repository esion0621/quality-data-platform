import { Layout, theme } from 'antd'

const { Header: AntHeader } = Layout

export default function Header() {
  const {
    token: { colorBgContainer },
  } = theme.useToken()

  return (
    <AntHeader style={{ padding: 0, background: colorBgContainer }}>
      <div style={{ marginLeft: 24, fontSize: 18, fontWeight: 'bold' }}>
        数据质量监控平台
      </div>
    </AntHeader>
  )
}
