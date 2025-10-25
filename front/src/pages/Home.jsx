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

            <div className="button-container">
              <button 
                type="submit" 
                className="btn btn-primary btn-fixed-width"
                disabled={loading}
              >
                {loading ? '创建中...' : '开始创作'}
              </button>
            </div>
          </form>
        </div>

        <div className="home-features">
          <h3>功能特色</h3>
          <div className="features-grid">
            <div className="feature-card">
              <svg className="feature-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <h4>文本转动漫</h4>
              <p>输入小说文本，自动生成动漫场景</p>
            </div>
            <div className="feature-card">
              <svg className="feature-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <h4>集数管理</h4>
              <p>支持创建多集内容，灵活管理</p>
            </div>
            <div className="feature-card">
              <svg className="feature-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <h4>付费观看</h4>
              <p>设置金币价格，支持付费内容</p>
            </div>
            <div className="feature-card">
              <svg className="feature-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
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
