import { Modal, Input } from 'antd'
import { useState } from 'react'

export default function AlertConfirmModal({ visible, onCancel, onConfirm }) {
  const [confirmBy, setConfirmBy] = useState('')

  const handleOk = () => {
    onConfirm(confirmBy)
    setConfirmBy('')
  }

  return (
    <Modal
      title="确认告警"
      open={visible}
      onOk={handleOk}
      onCancel={onCancel}
    >
      <Input 
        placeholder="请输入确认人" 
        value={confirmBy} 
        onChange={e => setConfirmBy(e.target.value)}
      />
    </Modal>
  )
}
