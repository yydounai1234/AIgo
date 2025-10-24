import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Navigation.css'

function Navigation({ userBalance }) {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useAuth()
  
  const isActive = (path) => {
    return location.pathname === path ? 'active' : ''
  }
  
  const handleLogout = () => {
    logout()
    navigate('/login')
  }
  
  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <Link to="/">AIgo 动漫生成</Link>
        </div>
        
        <div className="nav-links">
          <Link to="/" className={isActive('/')}>
            首页
          </Link>
          <Link to="/gallery" className={isActive('/gallery')}>
            作品广场
          </Link>
          {isAuthenticated() && (
            <Link to="/my-works" className={isActive('/my-works')}>
              我的作品
            </Link>
          )}
        </div>
        
        <div className="nav-user">
          {isAuthenticated() ? (
            <>
              <span className="user-info">
                👤 {user?.username}
              </span>
              <span className="coin-balance">
                💰 {userBalance} 金币
              </span>
              <button className="btn-logout" onClick={handleLogout}>
                退出
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-login">
                登录
              </Link>
              <Link to="/register" className="btn-register">
                注册
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

export default Navigation
