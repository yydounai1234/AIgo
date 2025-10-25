import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import './WorkDetail.css'

function WorkDetail() {
  const { workId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated } = useAuth()
  const [work, setWork] = useState(null)
  const [episodes, setEpisodes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login', { state: { from: location } })
      return
    }
    loadWorkDetail()
  }, [workId, isAuthenticated, navigate, location])

  const loadWorkDetail = async () => {
    setLoading(true)
    setError('')
    
    try {
      const result = await api.getWork(workId)
      
      if (result.success) {
        setWork({
          ...result.data,
          isLiked: result.data.isLiked === true
        })
        if (result.data.episodes) {
          setEpisodes(result.data.episodes)
        }
      } else {
        setError(result.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载作品详情时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handleLike = async () => {
    if (!isAuthenticated()) {
      navigate('/login', { state: { from: location } })
      return
    }

    try {
      if (work.isLiked) {
        const result = await api.unlikeWork(workId)
        if (result.success) {
          setWork({
            ...work,
            isLiked: false,
            likesCount: Math.max(0, (work.likesCount || 0) - 1)
          })
        }
      } else {
        const result = await api.likeWork(workId)
        if (result.success) {
          setWork({
            ...work,
            isLiked: true,
            likesCount: (work.likesCount || 0) + 1
          })
        }
      }
    } catch (err) {
      console.error('点赞操作失败', err)
    }
  }

  const handleViewEpisode = (episodeId) => {
    navigate(`/episode/${episodeId}`)
  }

  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  if (error) {
    return (
      <div className="error-page">
        <div className="error-container">
          <h2>加载失败</h2>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={() => navigate('/gallery')}>
            返回作品广场
          </button>
        </div>
      </div>
    )
  }

  if (!work) {
    return (
      <div className="error-page">
        <div className="error-container">
          <h2>作品不存在</h2>
          <button className="btn btn-primary" onClick={() => navigate('/gallery')}>
            返回作品广场
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="work-detail-page">
      <div className="work-detail-container">
        <button className="btn-back" onClick={() => navigate('/gallery')}>
          ← 返回作品广场
        </button>

        <div className="work-header">
          <div className="work-cover-large">
            {work.coverImage ? (
              <img src={work.coverImage} alt={work.title} />
            ) : (
              <div className="work-cover-placeholder-large">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
            )}
          </div>

          <div className="work-info">
            <h1 className="work-title">{work.title}</h1>
            
            {work.description && (
              <p className="work-description">{work.description}</p>
            )}

            <div className="work-stats">
              <div className="stat-item">
                <svg className="stat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span className="stat-label">集数</span>
                <span className="stat-value">{episodes.length || 0}</span>
              </div>
              <div className="stat-item">
                <svg className="stat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span className="stat-label">浏览</span>
                <span className="stat-value">{work.viewsCount || 0}</span>
              </div>
              <div className="stat-item">
                <svg className="stat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span className="stat-label">点赞</span>
                <span className="stat-value">{work.likesCount || 0}</span>
              </div>
            </div>

            <button
              onClick={handleLike}
              className={`btn-like-large ${work.isLiked ? 'liked' : ''}`}
            >
              <svg className="like-icon" viewBox="0 0 24 24" fill={work.isLiked ? "currentColor" : "none"} xmlns="http://www.w3.org/2000/svg">
                <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <span>{work.isLiked ? '已点赞' : '点赞'}</span>
            </button>
          </div>
        </div>

        <div className="episodes-section">
          <h2 className="episodes-title">集数列表</h2>
          
          {episodes.length === 0 ? (
            <div className="empty-state">
              <p>该作品还没有发布任何集数</p>
            </div>
          ) : (
            <div className="episodes-list">
              {episodes.map((episode) => (
                <div key={episode.id} className="episode-card">
                  <div className="episode-number">
                    第 {episode.episodeNumber} 集
                  </div>
                  
                  <div className="episode-content">
                    <h3 className="episode-title">{episode.title}</h3>
                    <p className="episode-description">{episode.novelText || '暂无简介'}</p>
                    
                    <div className="episode-meta">
                      {episode.isFree ? (
                        <span className="badge badge-free">免费</span>
                      ) : (
                        <span className="badge badge-paid">
                          <svg className="coin-icon-sm" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                          </svg>
                          {episode.coinPrice} 金币
                        </span>
                      )}
                    </div>
                  </div>

                  <button
                    onClick={() => handleViewEpisode(episode.id)}
                    className="btn btn-primary btn-sm btn-watch"
                  >
                    观看
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default WorkDetail
