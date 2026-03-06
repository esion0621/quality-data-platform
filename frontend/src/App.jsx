import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import RuleManagement from './pages/RuleManagement'
import TaskInstances from './pages/TaskInstances'
import Alerts from './pages/Alerts'
import RealtimeMonitor from './pages/RealtimeMonitor'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="rules" element={<RuleManagement />} />
          <Route path="tasks" element={<TaskInstances />} />
          <Route path="alerts" element={<Alerts />} />
          <Route path="realtime" element={<RealtimeMonitor />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
