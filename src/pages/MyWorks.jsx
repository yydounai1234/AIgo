import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './MyWorks.css'

function MyWorks() {
  const navigate = useNavigate()
  const [works, setWorks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadMyWorks()
  }, [])

  const loadMyWorks = async () => {
    setLoading(true)
    setError('')
    
    try {
      const result = await api.getMyWorks()
      
      if (result.success) {
        setWorks(result.data)
      } else {
        setError(result.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载我的作品时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handleEditWork = (workId) => {
    navigate(`/work/${workId}/edit`)
  }

  const handleDeleteWork = async (workId) => {
    if (!confirm('确定要删除这个作品吗？此操作不可撤销。')) {
      return
    }
    
    try {
      const result = await api.deleteWork(workId)
      
      if (result.success) {
        setWorks(works.filter(w => w.id !== workId))
        alert('作品已删除')
      } else {
        alert(result.error?.message || '删除失败')
      }
    } catch (err) {
      alert('删除作品时发生错误')
    }
  }

  const handleViewEpisode = (episodeId) => {
    navigate(`/episode/${episodeId}`)
  }

  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  return (
    <div className="my-works-page">
      <div className="my-works-container">
        <div className="page-header">
          <h1>我的作品</h1>
          <button
            onClick={() => navigate('/')}
            className="btn btn-primary"
          >
            + 创建新作品
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        {works.length === 0 ? (
          <div className="empty-state">
            <p>你还没有创建任何作品</p>
            <button
              onClick={() => navigate('/')}
              className="btn btn-primary"
            >
              立即创建
            </button>
          </div>
        ) : (
          <div className="works-grid">
            {works.map(work => (
              <div key={work.id} className="work-card">
                <div className="work-card-header">
                  <h3>{work.title}</h3>
                  <div className="work-status">
                    <span className={`badge ${work.isPublic ? 'badge-success' : 'badge-secondary'}`}>
                      {work.isPublic ? '已公开' : '未公开'}
                    </span>
                  </div>
                </div>

                {work.description && (
                  <p className="work-description">{work.description}</p>
                )}

                <div className="work-stats">
                  <span>📚 {work.episodes?.length || 0} 集</span>
                  <span>
                    ✓ {work.episodes?.filter(e => e.isPublished).length || 0} 已发布
                  </span>
                </div>

                <div className="episodes-preview">
                  {work.episodes && work.episodes.length > 0 ? (
                    <>
                      <h4>集数列表</h4>
                      <div className="episodes-list-compact">
                        {work.episodes.map(episode => (
                          <div key={episode.id} className="episode-preview-item">
                            <span className="episode-number">
                              第{episode.episodeNumber}集
                            </span>
                            <span className="episode-title">{episode.title}</span>
                            <div className="episode-badges">
                              <span className={`badge-sm ${episode.isPublished ? 'badge-success' : 'badge-warning'}`}>
                                {episode.isPublished ? '已发布' : '草稿'}
                              </span>
                              <span className={`badge-sm ${episode.isFree ? 'badge-info' : 'badge-primary'}`}>
                                {episode.isFree ? '免费' : `${episode.coinPrice}💰`}
                              </span>
                            </div>
                            <button
                              onClick={() => handleViewEpisode(episode.id)}
                              className="btn-link"
                            >
                              查看
                            </button>
                          </div>
                        ))}
                      </div>
                    </>
                  ) : (
                    <p className="no-episodes">还没有创建集数</p>
                  )}
                </div>

                <div className="work-actions">
                  <button
                    onClick={() => handleEditWork(work.id)}
                    className="btn btn-secondary"
                  >
                    编辑管理
                  </button>
                  <button
                    onClick={() => handleDeleteWork(work.id)}
                    className="btn btn-danger"
                  >
                    删除
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MyWorks
