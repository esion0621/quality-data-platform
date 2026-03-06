import { Layout as AntLayout } from 'antd'
import { Outlet } from 'react-router-dom'
import Header from './Header'
import SideMenu from './SideMenu'

const { Content } = AntLayout

export default function Layout() {
  return (
    <AntLayout>
      <SideMenu />
      <AntLayout>
        <Header />
        <Content>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  )
}
