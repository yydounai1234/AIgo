import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import './Gallery.css'

function MyFavorites() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated } = useAuth()
  const [works, setWorks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadFavorites()
  }, [])

  const loadFavorites = async () => {
    setLoading(true)
    setError('')
    
    try {
      const result = await api.getMyFavorites()
      
      if (result.success) {
        setWorks(result.data)
      } else {
        setError(result.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载我的收藏时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handleUnlike = async (workId) => {
    try {
      const result = await api.unlikeWork(workId)
      if (result.success) {
        setWorks(works.filter(w => w.id !== workId))
      }
    } catch (err) {
      console.error('取消点赞失败', err)
    }
  }

  const handleViewWork = (workId) => {
    if (!isAuthenticated()) {
      navigate('/login', { state: { from: location } })
      return
    }
    navigate(`/work/${workId}`)
  }

  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  return (
    <div className="gallery-page">
      <div className="gallery-container">
        <div className="gallery-header">
          <h1>我的收藏</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        {works.length === 0 ? (
          <div className="empty-state">
            <p>还没有收藏任何作品</p>
          </div>
        ) : (
          <div className="gallery-grid">
            {works.map(work => (
              <div key={work.id} className="gallery-work-card">
                <div className="work-cover">
                  {work.coverImage ? (
                    <img src={work.coverImage} alt={work.title} />
                  ) : (
                    <div className="work-cover-placeholder">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    </div>
                  )}
                </div>

                <div className="work-content">
                  <h3>{work.title}</h3>
                  {work.description && (
                    <p className="work-description">{work.description}</p>
                  )}

                  <div className="work-meta">
                    <span className="work-episodes">
                      <svg className="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                      {work.episodeCount || 0} 集
                    </span>
                    <span className="work-views">
                      <svg className="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                      {work.viewsCount || 0} 浏览
                    </span>
                  </div>

                  <div className="work-footer">
                    <button
                      onClick={() => handleUnlike(work.id)}
                      className="btn-like liked"
                    >
                      <svg className="like-icon" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                        <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                      <span className="like-count">{work.likesCount || 0}</span>
                    </button>

                    <button
                      onClick={() => handleViewWork(work.id)}
                      className="btn btn-primary btn-sm"
                    >
                      查看作品
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MyFavorites
