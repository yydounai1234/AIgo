import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import './EpisodeViewer.css'

function EpisodeViewer() {
  const { episodeId } = useParams()
  const navigate = useNavigate()
  
  const [episode, setEpisode] = useState(null)
  const [work, setWork] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [needsPurchase, setNeedsPurchase] = useState(false)
  const [purchasing, setPurchasing] = useState(false)
  const [currentScene, setCurrentScene] = useState(0)

  useEffect(() => {
    loadEpisode()
  }, [episodeId])

  const loadEpisode = async () => {
    setLoading(true)
    setError('')
    setNeedsPurchase(false)
    
    try {
      const result = await api.getEpisode(episodeId)
      
      if (result.needsPurchase) {
        setNeedsPurchase(true)
        setEpisode(result.data)
      } else if (result.success) {
        setEpisode(result.data)
        
        if (result.data.workId) {
          const workResult = await api.getWork(result.data.workId)
          if (workResult.success) {
            setWork(workResult.data)
          }
        }
      } else {
        setError(result.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载集数时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handlePurchase = async () => {
    if (!episode) return
    
    setPurchasing(true)
    
    try {
      const result = await api.purchaseEpisode(episodeId)
      
      if (result.success) {
        alert(`购买成功！消耗 ${result.data.coinCost} 金币，剩余 ${result.data.newBalance} 金币`)
        await loadEpisode()
      } else {
        alert(result.error?.message || '购买失败')
      }
    } catch (err) {
      alert('购买时发生错误')
    } finally {
      setPurchasing(false)
    }
  }

  const handlePrevScene = () => {
    if (currentScene > 0) {
      setCurrentScene(currentScene - 1)
    }
  }

  const handleNextScene = () => {
    if (episode?.scenes && currentScene < episode.scenes.length - 1) {
      setCurrentScene(currentScene + 1)
    }
  }

  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  if (needsPurchase && episode) {
    return (
      <div className="episode-viewer-page">
        <div className="purchase-prompt">
          <div className="purchase-card">
            <h2>🔒 付费内容</h2>
            <h3>{episode.title}</h3>
            <p className="price-info">
              需要支付 <strong>{episode.coinPrice}</strong> 金币才能观看此集内容
            </p>
            <p className="exchange-info">
              (100 金币 = 1 元)
            </p>
            <div className="purchase-actions">
              <button
                onClick={handlePurchase}
                className="btn btn-primary btn-large"
                disabled={purchasing}
              >
                {purchasing ? '购买中...' : `购买 (${episode.coinPrice} 💰)`}
              </button>
              <button
                onClick={() => navigate(-1)}
                className="btn btn-secondary"
              >
                返回
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (error || !episode) {
    return (
      <div className="error-page">
        <p>{error || '集数不存在'}</p>
        <button onClick={() => navigate(-1)} className="btn btn-secondary">
          返回
        </button>
      </div>
    )
  }

  const scenes = episode.scenes || []
  const currentSceneData = scenes[currentScene]

  return (
    <div className="episode-viewer-page">
      <div className="viewer-container">
        <div className="viewer-header">
          <button onClick={() => navigate(-1)} className="btn-back">
            ← 返回
          </button>
          <div className="episode-info">
            {work && <span className="work-title">{work.title}</span>}
            <h2>第{episode.episodeNumber}集：{episode.title}</h2>
          </div>
          <div className="episode-meta">
            {episode.isFree ? (
              <span className="badge badge-success">免费</span>
            ) : (
              <span className="badge badge-primary">{episode.coinPrice} 💰</span>
            )}
          </div>
        </div>

        {scenes.length === 0 ? (
          <div className="no-scenes">
            <p>此集还没有场景内容</p>
          </div>
        ) : (
          <>
            <div className="scene-viewer">
              <div className="scene-image">
                {currentSceneData?.imageUrl ? (
                  <img src={currentSceneData.imageUrl} alt={`场景 ${currentScene + 1}`} />
                ) : (
                  <div className="scene-placeholder">
                    <span>🎬</span>
                    <p>场景 {currentScene + 1}</p>
                  </div>
                )}
              </div>
              
              <div className="scene-text">
                <div className="scene-number">
                  场景 {currentScene + 1} / {scenes.length}
                </div>
                <p className="scene-content">{currentSceneData?.text}</p>
              </div>
            </div>

            <div className="viewer-controls">
              <button
                onClick={handlePrevScene}
                className="btn btn-control"
                disabled={currentScene === 0}
              >
                ← 上一个场景
              </button>
              
              <div className="scene-progress">
                <div className="progress-bar">
                  <div 
                    className="progress-fill"
                    style={{ width: `${((currentScene + 1) / scenes.length) * 100}%` }}
                  />
                </div>
                <span className="progress-text">
                  {currentScene + 1} / {scenes.length}
                </span>
              </div>
              
              <button
                onClick={handleNextScene}
                className="btn btn-control"
                disabled={currentScene === scenes.length - 1}
              >
                下一个场景 →
              </button>
            </div>

            <div className="scene-thumbnails">
              {scenes.map((scene, index) => (
                <div
                  key={index}
                  className={`thumbnail ${index === currentScene ? 'active' : ''}`}
                  onClick={() => setCurrentScene(index)}
                >
                  <div className="thumbnail-number">{index + 1}</div>
                  {scene.imageUrl ? (
                    <img src={scene.imageUrl} alt={`场景 ${index + 1}`} />
                  ) : (
                    <div className="thumbnail-placeholder">🎬</div>
                  )}
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default EpisodeViewer
