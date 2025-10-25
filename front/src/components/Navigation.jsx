import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import './Navigation.css'

function Navigation({ userBalance }) {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  
  const isActive = (path) => {
    return location.pathname === path ? 'active' : ''
  }
  
  const handleLogout = () => {
    logout()
    navigate('/login')
    setMobileMenuOpen(false)
  }
  
  const handleLinkClick = () => {
    setMobileMenuOpen(false)
  }
  
  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <Link to="/" onClick={handleLinkClick}>AIgo 动漫生成</Link>
        </div>
        
        <button 
          className="mobile-menu-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            {mobileMenuOpen ? (
              <path d="M6 18L18 6M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            ) : (
              <path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            )}
          </svg>
        </button>
        
        <div className={`nav-links ${mobileMenuOpen ? 'mobile-open' : ''}`}>
          <Link to="/" className={isActive('/')} onClick={handleLinkClick}>
            首页
          </Link>
          <Link to="/gallery" className={isActive('/gallery')} onClick={handleLinkClick}>
            作品广场
          </Link>
          {isAuthenticated() && (
            <Link to="/my-works" className={isActive('/my-works')} onClick={handleLinkClick}>
              我的作品
            </Link>
          )}
        </div>
        
        <div className={`nav-user ${mobileMenuOpen ? 'mobile-open' : ''}`}>
          {isAuthenticated() ? (
            <>
              <span className="user-info">
                <svg className="user-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="2"/>
                  <path d="M4 20C4 16.6863 6.68629 14 10 14H14C17.3137 14 20 16.6863 20 20" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                {user?.username}
              </span>
              <span className="coin-balance">
                <svg className="coin-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                  <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                {userBalance} 金币
              </span>
              <button className="btn-logout" onClick={handleLogout}>
                退出
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-login" onClick={handleLinkClick}>
                登录
              </Link>
              <Link to="/register" className="btn-register" onClick={handleLinkClick}>
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
