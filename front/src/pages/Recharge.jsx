import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import Modal from '../components/Modal'
import './Recharge.css'

function Recharge() {
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [modal, setModal] = useState({ isOpen: false, type: 'alert', title: '', message: '' })

  const presetAmounts = [10, 50, 100, 200, 500, 1000]

  const handleRecharge = async () => {
    const rechargeAmount = parseInt(amount)
    
    if (!rechargeAmount || rechargeAmount <= 0) {
      setModal({
        isOpen: true,
        type: 'alert',
        title: '输入错误',
        message: '请输入有效的充值金额'
      })
      return
    }

    if (rechargeAmount > 1000) {
      setModal({
        isOpen: true,
        type: 'alert',
        title: '充值限制',
        message: '单次充值金额不能超过 1000 金币'
      })
      return
    }

    setLoading(true)
    try {
      const result = await api.rechargeCoins(rechargeAmount)
      
      if (result.success) {
        updateUser({ ...user, coinBalance: result.data.newBalance })
        setModal({
          isOpen: true,
          type: 'alert',
          title: '充值成功',
          message: `成功充值 ${rechargeAmount} 金币，当前余额：${result.data.newBalance} 金币`
        })
        setAmount('')
      } else {
        setModal({
          isOpen: true,
          type: 'alert',
          title: '充值失败',
          message: result.error?.message || '充值失败，请稍后重试'
        })
      }
    } catch (err) {
      setModal({
        isOpen: true,
        type: 'alert',
        title: '错误',
        message: '充值时发生错误，请稍后重试'
      })
    } finally {
      setLoading(false)
    }
  }

  const handlePresetClick = (preset) => {
    setAmount(preset.toString())
  }

  const handleModalClose = () => {
    setModal({ ...modal, isOpen: false })
    if (modal.title === '充值成功') {
      navigate(-1)
    }
  }

  return (
    <div className="recharge-page">
      <div className="recharge-container">
        <div className="recharge-header">
          <button onClick={() => navigate(-1)} className="btn-back">
            ← 返回
          </button>
          <h2>金币充值</h2>
        </div>

        <div className="recharge-card">
          <div className="balance-display">
            <div className="balance-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <div className="balance-info">
              <span className="balance-label">当前余额</span>
              <span className="balance-amount">{user?.coinBalance || 0} 金币</span>
            </div>
          </div>

          <div className="recharge-notice">
            <p>
              <svg className="notice-icon icon-info" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 16v-4M12 8h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              提示：目前为测试阶段，支付功能尚未打通，您可以免费充值金币用于体验功能
            </p>
            <p>
              <svg className="notice-icon icon-warning" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 20h20L12 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                <path d="M12 9v4M12 17h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              单次充值上限：1000 金币
            </p>
          </div>

          <div className="preset-amounts">
            <h3>快速充值</h3>
            <div className="preset-grid">
              {presetAmounts.map(preset => (
                <button
                  key={preset}
                  className={`preset-btn ${amount === preset.toString() ? 'active' : ''}`}
                  onClick={() => handlePresetClick(preset)}
                >
                  <span className="preset-amount">{preset}</span>
                  <span className="preset-label">金币</span>
                </button>
              ))}
            </div>
          </div>

          <div className="custom-amount">
            <h3>自定义金额</h3>
            <div className="amount-input-group">
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="请输入充值金额"
                min="1"
                max="1000"
                className="amount-input"
              />
              <span className="input-suffix">金币</span>
            </div>
          </div>

          <div className="recharge-action">
            <button
              onClick={handleRecharge}
              disabled={loading || !amount}
              className="btn btn-primary btn-large"
            >
              {loading ? '充值中...' : '立即充值'}
            </button>
          </div>

          <div className="recharge-info">
            <h4>充值说明</h4>
            <ul>
              <li>金币可用于购买付费集数内容</li>
              <li>100 金币 = 1 元（测试阶段免费）</li>
              <li>充值金币不支持退款</li>
              <li>如有问题，请联系客服</li>
            </ul>
          </div>
        </div>
      </div>

      <Modal
        isOpen={modal.isOpen}
        onClose={handleModalClose}
        title={modal.title}
        message={modal.message}
        type={modal.type}
      />
    </div>
  )
}

export default Recharge
