import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './Gallery.css'

function Gallery() {
  const navigate = useNavigate()
  const [works, setWorks] = useState([])
  const [sortBy, setSortBy] = useState('latest')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadGallery()
  }, [sortBy])

  const loadGallery = async () => {
    setLoading(true)
    setError('')
    
    try {
      const result = await api.getGallery({ sortBy })
      
      if (result.success) {
        setWorks(result.data)
      } else {
        setError(result.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载作品广场时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handleLike = async (workId) => {
    const work = works.find(w => w.id === workId)
    if (!work) return
    
    try {
      if (work.isLiked) {
        const result = await api.unlikeWork(workId)
        if (result.success) {
          setWorks(works.map(w => 
            w.id === workId 
              ? { ...w, isLiked: false, likesCount: Math.max(0, (w.likesCount || 0) - 1) }
              : w
          ))
        }
      } else {
        const result = await api.likeWork(workId)
        if (result.success) {
          setWorks(works.map(w => 
            w.id === workId 
              ? { ...w, isLiked: true, likesCount: (w.likesCount || 0) + 1 }
              : w
          ))
        }
      }
    } catch (err) {
      console.error('点赞操作失败', err)
    }
  }

  const handleViewWork = (workId) => {
    navigate(`/work/${workId}`)
  }

  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  return (
    <div className="gallery-page">
      <div className="gallery-container">
        <div className="gallery-header">
          <h1>作品广场</h1>
          <div className="sort-controls">
            <label>排序：</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="latest">最新发布</option>
              <option value="likes">最多点赞</option>
            </select>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {works.length === 0 ? (
          <div className="empty-state">
            <p>还没有公开的作品</p>
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
                      <span>📚</span>
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
                      📖 {work.episodeCount || 0} 集
                    </span>
                    <span className="work-views">
                      👁️ {work.viewsCount || 0} 浏览
                    </span>
                  </div>

                  <div className="work-footer">
                    <button
                      onClick={() => handleLike(work.id)}
                      className={`btn-like ${work.isLiked ? 'liked' : ''}`}
                    >
                      <span className="like-icon">{work.isLiked ? '❤️' : '🤍'}</span>
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

export default Gallery
