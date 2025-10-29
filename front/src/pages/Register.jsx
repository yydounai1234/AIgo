import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import AvatarSelector from '../components/AvatarSelector'
import './Register.css'

function Register() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showAvatarSelector, setShowAvatarSelector] = useState(false)
  const [registeredUser, setRegisteredUser] = useState(null)
  const [registeredToken, setRegisteredToken] = useState(null)
  
  const { login } = useAuth()
  const navigate = useNavigate()
  
  const validateForm = () => {
    if (!username.trim()) {
      setError('请输入用户名')
      return false
    }
    
    if (username.length < 3 || username.length > 20) {
      setError('用户名长度应为 3-20 个字符')
      return false
    }
    
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      setError('用户名只能包含字母、数字和下划线')
      return false
    }
    
    if (!email.trim()) {
      setError('请输入邮箱地址')
      return false
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email)) {
      setError('请输入有效的邮箱地址')
      return false
    }
    
    if (!password) {
      setError('请输入密码')
      return false
    }
    
    if (password.length < 8 || password.length > 50) {
      setError('密码长度应为 8-50 个字符')
      return false
    }
    
    if (password !== confirmPassword) {
      setError('两次输入的密码不一致')
      return false
    }
    
    return true
  }
  
  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    if (!validateForm()) {
      return
    }
    
    setLoading(true)
    
    try {
      const result = await api.register(username, email, password)
      
      if (result.success) {
        setRegisteredUser(result.data.user)
        setRegisteredToken(result.data.token)
        setShowAvatarSelector(true)
        setLoading(false)
      } else {
        setError(result.error?.message || '注册失败')
        setLoading(false)
      }
    } catch (err) {
      setError('注册失败，请稍后重试')
      console.error('Register error:', err)
      setLoading(false)
    }
  }

  const handleAvatarConfirm = async (avatarData) => {
    setLoading(true)
    setShowAvatarSelector(false)
    
    try {
      const result = await api.uploadAvatar(avatarData)
      
      if (result.success) {
        const updatedUser = { ...registeredUser, avatarUrl: result.data.avatarUrl }
        login(updatedUser, registeredToken)
        navigate('/', { replace: true })
      } else {
        console.error('头像上传失败:', result.error)
        setError('头像上传失败，请重试')
        setShowAvatarSelector(true)
        setLoading(false)
      }
    } catch (err) {
      console.error('头像上传失败:', err)
      setError('头像上传失败，请重试')
      setShowAvatarSelector(true)
      setLoading(false)
    }
  }

  const handleAvatarCancel = () => {
    setShowAvatarSelector(false)
    login(registeredUser, registeredToken)
    navigate('/', { replace: true })
  }
  
  return (
    <>
      {showAvatarSelector && (
        <AvatarSelector
          onConfirm={handleAvatarConfirm}
          onError={setError}
          onCancel={handleAvatarCancel}
        />
      )}
      
      <div className="register-container">
        <div className="register-box">
          <h1>用户注册</h1>
          
          <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="3-20个字符，字母数字下划线"
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="email">邮箱地址</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="请输入有效的邮箱地址"
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">密码</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="8-50个字符"
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="confirmPassword">确认密码</label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="请再次输入密码"
              disabled={loading}
            />
          </div>
          
          {error && <div className="error-message">{error}</div>}
          
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? '注册中...' : '注册'}
          </button>
        </form>
        
        <div className="register-footer">
          <p>
            已有账号？ <Link to="/login">立即登录</Link>
          </p>
        </div>
        </div>
      </div>
    </>
  )
}

export default Register
