import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './Home.css'

function Home() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleCreateWork = async (e) => {
    e.preventDefault()
    
    if (!title.trim()) {
      setError('请输入作品标题')
      return
    }
    
    setLoading(true)
    setError('')
    
    try {
      const result = await api.createWork({
        title: title.trim(),
        description: description.trim(),
        isPublic: false
      })
      
      if (result.success) {
        navigate(`/work/${result.data.id}/edit`)
      } else {
        setError(result.error?.message || '创建失败')
      }
    } catch (err) {
      setError('创建作品时发生错误')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="home-page">
      <div className="home-hero">
        <h1>创作你的动漫世界</h1>
        <p>输入小说文本，AI 自动生成精美动漫场景</p>
      </div>

      <div className="home-content">
        <div className="create-work-card">
          <h2>创建新作品</h2>
          <form onSubmit={handleCreateWork}>
            <div className="form-group">
              <label htmlFor="title">作品标题 *</label>
              <input
                id="title"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="例如：魔法学院奇遇记"
                maxLength={100}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">作品简介</label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="简单介绍一下你的作品..."
                rows={4}
                maxLength={500}
                disabled={loading}
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? '创建中...' : '开始创作'}
            </button>
          </form>
        </div>

        <div className="home-features">
          <h3>功能特色</h3>
          <div className="features-grid">
            <div className="feature-card">
              <span className="feature-icon">📝</span>
              <h4>文本转动漫</h4>
              <p>输入小说文本，自动生成动漫场景</p>
            </div>
            <div className="feature-card">
              <span className="feature-icon">📚</span>
              <h4>集数管理</h4>
              <p>支持创建多集内容，灵活管理</p>
            </div>
            <div className="feature-card">
              <span className="feature-icon">💰</span>
              <h4>付费观看</h4>
              <p>设置金币价格，支持付费内容</p>
            </div>
            <div className="feature-card">
              <span className="feature-icon">🌐</span>
              <h4>作品广场</h4>
              <p>公开作品，与他人分享创作</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Home
