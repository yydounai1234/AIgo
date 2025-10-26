import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import './Login.css'

function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  
  const from = location.state?.returnTo || location.state?.from?.pathname || '/'
  
  const validateForm = () => {
    if (!username.trim()) {
      setError('请输入用户名')
      return false
    }
    if (!password) {
      setError('请输入密码')
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
      const result = await api.login(username, password)
      
      if (result.success) {
        login(result.data.user, result.data.token)
        navigate(from, { replace: true })
      } else {
        setError(result.error?.message || '登录失败')
      }
    } catch (err) {
      setError('登录失败，请稍后重试')
      console.error('Login error:', err)
    } finally {
      setLoading(false)
    }
  }
  
  return (
    <div className="login-container">
      <div className="login-box">
        <h1>用户登录</h1>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名"
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
              placeholder="请输入密码"
              disabled={loading}
            />
          </div>
          
          {error && <div className="error-message">{error}</div>}
          
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? '登录中...' : '登录'}
          </button>
        </form>
        
        <div className="login-footer">
          <p>
            还没有账号？ <Link to="/register">立即注册</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login
