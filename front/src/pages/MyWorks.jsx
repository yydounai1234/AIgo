import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import Modal from '../components/Modal'
import './MyWorks.css'

function MyWorks() {
  const navigate = useNavigate()
  const [works, setWorks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState({ isOpen: false, type: 'alert', title: '', message: '', onConfirm: null })
  const [deletingWorkId, setDeletingWorkId] = useState(null)

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

  const handleDeleteWork = (workId) => {
    setDeletingWorkId(workId)
    setModal({
      isOpen: true,
      type: 'confirm',
      title: '确认删除',
      message: '确定要删除这个作品吗？此操作不可撤销。',
      onConfirm: () => confirmDeleteWork(workId)
    })
  }

  const confirmDeleteWork = async (workId) => {
    setModal({ ...modal, isOpen: false })
    
    try {
      const result = await api.deleteWork(workId)
      
      if (result.success) {
        setWorks(works.filter(w => w.id !== workId))
        setModal({ isOpen: true, type: 'alert', title: '成功', message: '作品已删除', onConfirm: null })
      } else {
        setModal({ isOpen: true, type: 'alert', title: '错误', message: result.error?.message || '删除失败', onConfirm: null })
      }
    } catch (err) {
      setModal({ isOpen: true, type: 'alert', title: '错误', message: '删除作品时发生错误', onConfirm: null })
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
          <div className="page-header-left">
            <h1>我的作品</h1>
            <div className="h5-favorites-link">
              <button
                onClick={() => navigate('/my-favorites')}
                className="btn-favorites-link"
              >
                <svg className="favorites-icon" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                </svg>
                我的收藏
              </button>
            </div>
          </div>
          <button
            onClick={() => navigate('/')}
            className="btn btn-primary btn-fixed-width"
          >
            + 创建新作品
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        {works.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-image">
              <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{ stopColor: '#667eea', stopOpacity: 0.2 }} />
                    <stop offset="100%" style={{ stopColor: '#764ba2', stopOpacity: 0.2 }} />
                  </linearGradient>
                </defs>
                <circle cx="100" cy="100" r="80" fill="url(#gradient1)" />
                <path d="M70 90 L70 70 L90 70 M130 70 L130 90 M70 130 Q100 110 130 130" 
                      stroke="#667eea" strokeWidth="4" fill="none" strokeLinecap="round" />
                <circle cx="80" cy="80" r="5" fill="#667eea" />
                <circle cx="120" cy="80" r="5" fill="#667eea" />
                <path d="M60 140 L140 140 L130 160 L70 160 Z" fill="#764ba2" opacity="0.3" />
              </svg>
            </div>
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
                  <span>
                    <svg className="stat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    {work.episodes?.length || 0} 集
                  </span>
                  <span>
                    <svg className="stat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    {work.episodes?.filter(e => e.isPublished).length || 0} 已发布
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
                                {episode.isFree ? '免费' : (
                                  <>
                                    <svg className="coin-icon-sm" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                                      <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    </svg>
                                    {episode.coinPrice}
                                  </>
                                )}
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

      <Modal
        isOpen={modal.isOpen}
        onClose={() => setModal({ ...modal, isOpen: false })}
        onConfirm={modal.onConfirm}
        title={modal.title}
        message={modal.message}
        type={modal.type}
      />
    </div>
  )
}

export default MyWorks
