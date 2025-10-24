import { Link, useLocation } from 'react-router-dom'
import './Navigation.css'

function Navigation({ userBalance }) {
  const location = useLocation()
  
  const isActive = (path) => {
    return location.pathname === path ? 'active' : ''
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
          <Link to="/my-works" className={isActive('/my-works')}>
            我的作品
          </Link>
          <Link to="/gallery" className={isActive('/gallery')}>
            作品广场
          </Link>
        </div>
        
        <div className="nav-user">
          <span className="coin-balance">
            💰 {userBalance} 金币
          </span>
        </div>
      </div>
    </nav>
  )
}

export default Navigation
