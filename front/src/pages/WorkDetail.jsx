import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import './WorkDetail.css'

function WorkDetail() {
  const { workId } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const [work, setWork] = useState(null)
  const [episodes, setEpisodes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadWorkDetail()
  }, [workId])

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
      navigate('/login')
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
                <span>📚</span>
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
                <span className="stat-icon">📖</span>
                <span className="stat-label">集数</span>
                <span className="stat-value">{episodes.length || 0}</span>
              </div>
              <div className="stat-item">
                <span className="stat-icon">👁️</span>
                <span className="stat-label">浏览</span>
                <span className="stat-value">{work.viewsCount || 0}</span>
              </div>
              <div className="stat-item">
                <span className="stat-icon">❤️</span>
                <span className="stat-label">点赞</span>
                <span className="stat-value">{work.likesCount || 0}</span>
              </div>
            </div>

            <button
              onClick={handleLike}
              className={`btn-like-large ${work.isLiked ? 'liked' : ''}`}
            >
              <span className="like-icon">{work.isLiked ? '❤️' : '🤍'}</span>
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
                    
                    <div className="episode-meta">
                      {episode.isFree ? (
                        <span className="badge badge-free">免费</span>
                      ) : (
                        <span className="badge badge-paid">
                          🪙 {episode.coinPrice} 金币
                        </span>
                      )}
                    </div>
                  </div>

                  <button
                    onClick={() => handleViewEpisode(episode.id)}
                    className="btn btn-primary btn-sm"
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
