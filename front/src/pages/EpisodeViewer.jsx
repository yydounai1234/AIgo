import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import Modal from '../components/Modal'
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
  const [retrying, setRetrying] = useState(false)
  const [currentScene, setCurrentScene] = useState(0)
  const [autoPlay, setAutoPlay] = useState(true)
  const [isPlaying, setIsPlaying] = useState(false)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const [showControls, setShowControls] = useState(true)
  const [modal, setModal] = useState({ isOpen: false, type: 'alert', title: '', message: '', onConfirm: null })
  const pollingIntervalRef = useRef(null)
  const audioRef = useRef(null)
  const imagePreloadRefs = useRef({})
  const viewerContainerRef = useRef(null)
  const hideControlsTimeoutRef = useRef(null)
  const shouldAutoPlayNextRef = useRef(false)

  useEffect(() => {
    loadEpisode()
    
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement)
    }
    
    document.addEventListener('fullscreenchange', handleFullscreenChange)
    
    return () => {
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current)
      }
      document.removeEventListener('fullscreenchange', handleFullscreenChange)
    }
  }, [episodeId])

  useEffect(() => {
    if (episode?.status === 'PENDING' || episode?.status === 'PROCESSING') {
      startPolling()
    } else {
      stopPolling()
    }
    
    return () => stopPolling()
  }, [episode?.status])

  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.pause()
      audioRef.current.currentTime = 0
      setIsPlaying(false)
    }
    
    const handleAudioEnded = () => {
      setIsPlaying(false)
      if (autoPlay && episode?.scenes && currentScene < episode.scenes.length - 1) {
        shouldAutoPlayNextRef.current = true
        setCurrentScene(prev => prev + 1)
      }
    }
    
    const handlePlay = () => setIsPlaying(true)
    const handlePause = () => setIsPlaying(false)
    
    const currentSceneData = episode?.scenes?.[currentScene]
    
    if (currentSceneData?.audioUrl && currentSceneData?.text !== '无') {
      const audioUrl = currentSceneData.audioUrl
      if (audioRef.current) {
        audioRef.current.src = audioUrl
        audioRef.current.load()
        
        audioRef.current.addEventListener('ended', handleAudioEnded)
        audioRef.current.addEventListener('play', handlePlay)
        audioRef.current.addEventListener('pause', handlePause)
        
        audioRef.current.addEventListener('canplaythrough', () => {
          console.log('Audio ready to play for scene', currentScene)
          
          if (shouldAutoPlayNextRef.current && autoPlay) {
            const playPromise = audioRef.current.play()
            if (playPromise !== undefined) {
              playPromise
                .then(() => {
                  setIsPlaying(true)
                  shouldAutoPlayNextRef.current = false
                })
                .catch(err => {
                  console.warn('Auto-play failed:', err)
                  shouldAutoPlayNextRef.current = false
                })
            }
          }
        }, { once: true })
      }
    }
    
    preloadAdjacentImages()
    
    return () => {
      if (audioRef.current) {
        audioRef.current.pause()
        audioRef.current.removeEventListener('ended', handleAudioEnded)
        audioRef.current.removeEventListener('play', handlePlay)
        audioRef.current.removeEventListener('pause', handlePause)
      }
    }
  }, [currentScene, episode?.scenes, autoPlay])

  const preloadAdjacentImages = () => {
    if (!episode?.scenes) return
    
    const imagesToPreload = []
    if (currentScene > 0) {
      imagesToPreload.push(currentScene - 1)
    }
    if (currentScene < episode.scenes.length - 1) {
      imagesToPreload.push(currentScene + 1)
    }
    
    imagesToPreload.forEach(index => {
      const scene = episode.scenes[index]
      if (scene?.imageUrl && !imagePreloadRefs.current[index]) {
        const img = new Image()
        img.src = scene.imageUrl
        imagePreloadRefs.current[index] = img
      }
    })
  }

  const startPolling = () => {
    if (pollingIntervalRef.current) return
    
    pollingIntervalRef.current = setInterval(() => {
      loadEpisodeQuietly()
    }, 3000)
  }

  const stopPolling = () => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current)
      pollingIntervalRef.current = null
    }
  }

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

  const loadEpisodeQuietly = async () => {
    try {
      const result = await api.getEpisode(episodeId)
      
      if (result.success) {
        setEpisode(result.data)
      }
    } catch (err) {
      console.error('Polling error:', err)
    }
  }

  const handleRetry = async () => {
    setRetrying(true)
    
    try {
      const result = await api.retryEpisode(episodeId)
      
      if (result.success) {
        setEpisode(result.data)
        setModal({ 
          isOpen: true, 
          type: 'alert', 
          title: '重新生成中', 
          message: '已开始重新生成集数内容，请稍候...', 
          onConfirm: null 
        })
      } else {
        setModal({ 
          isOpen: true, 
          type: 'alert', 
          title: '重试失败', 
          message: result.error?.message || '重试失败', 
          onConfirm: null 
        })
      }
    } catch (err) {
      setModal({ 
        isOpen: true, 
        type: 'alert', 
        title: '错误', 
        message: '重试时发生错误', 
        onConfirm: null 
      })
    } finally {
      setRetrying(false)
    }
  }

  const handlePurchase = async () => {
    if (!episode) return
    
    setPurchasing(true)
    
    try {
      const result = await api.purchaseEpisode(episodeId)
      
      if (result.success) {
        setModal({ 
          isOpen: true, 
          type: 'alert', 
          title: '购买成功', 
          message: `消耗 ${result.data.coinCost} 金币，剩余 ${result.data.newBalance} 金币`, 
          onConfirm: null 
        })
        await loadEpisode()
      } else {
        setModal({ 
          isOpen: true, 
          type: 'alert', 
          title: '购买失败', 
          message: result.error?.message || '购买失败', 
          onConfirm: null 
        })
      }
    } catch (err) {
      setModal({ 
        isOpen: true, 
        type: 'alert', 
        title: '错误', 
        message: '购买时发生错误', 
        onConfirm: null 
      })
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

  const toggleFullscreen = async () => {
    try {
      if (!document.fullscreenElement) {
        await viewerContainerRef.current?.requestFullscreen()
      } else {
        await document.exitFullscreen()
      }
    } catch (err) {
      console.error('Fullscreen error:', err)
    }
  }

  const togglePlayPause = () => {
    if (!audioRef.current) return
    
    const currentSceneData = episode?.scenes?.[currentScene]
    if (!currentSceneData?.audioUrl || currentSceneData?.text === '无') {
      console.warn('No audio available for current scene')
      return
    }
    
    if (isPlaying) {
      audioRef.current.pause()
      setIsPlaying(false)
    } else {
      if (!audioRef.current.src || audioRef.current.src === '') {
        audioRef.current.src = currentSceneData.audioUrl
        audioRef.current.load()
      }
      
      const playPromise = audioRef.current.play()
      if (playPromise !== undefined) {
        playPromise.then(() => {
          setIsPlaying(true)
        }).catch(err => {
          console.warn('Play failed:', err)
          setIsPlaying(false)
        })
      }
    }
  }

  const resetHideControlsTimer = () => {
    if (hideControlsTimeoutRef.current) {
      clearTimeout(hideControlsTimeoutRef.current)
    }

    setShowControls(true)

    if (isFullscreen) {
      hideControlsTimeoutRef.current = setTimeout(() => {
        setShowControls(false)
      }, 3000)
    }
  }

  const handleMouseMove = () => {
    if (isFullscreen) {
      resetHideControlsTimer()
    }
  }

  useEffect(() => {
    if (isFullscreen) {
      resetHideControlsTimer()
    } else {
      setShowControls(true)
      if (hideControlsTimeoutRef.current) {
        clearTimeout(hideControlsTimeoutRef.current)
      }
    }

    return () => {
      if (hideControlsTimeoutRef.current) {
        clearTimeout(hideControlsTimeoutRef.current)
      }
    }
  }, [isFullscreen])

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

  if (episode.status === 'PENDING' || episode.status === 'PROCESSING') {
    return (
      <div className="episode-viewer-page">
        <div className="generation-status">
          <div className="status-card">
            <div className="status-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="2"/>
                <path d="M4 20C4 16.6863 6.68629 14 10 14H14C17.3137 14 20 16.6863 20 20" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h2>🎬 生成中...</h2>
            <h3>{episode.title}</h3>
            <p className="status-message">
              {episode.status === 'PENDING' 
                ? '等待处理，请稍候...' 
                : '正在使用 AI 生成动漫内容，这可能需要几分钟时间...'}
            </p>
            <p className="status-tip">
              页面将自动刷新，请不要关闭窗口
            </p>
            <button onClick={() => navigate(-1)} className="btn btn-secondary">
              返回
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (episode.status === 'FAILED') {
    return (
      <div className="episode-viewer-page">
        <div className="generation-status">
          <div className="status-card error">
            <h2>❌ 生成失败</h2>
            <h3>{episode.title}</h3>
            <p className="error-message">
              {episode.errorMessage || '生成过程中发生错误'}
            </p>
            <div className="status-actions">
              <button
                onClick={handleRetry}
                className="btn btn-primary"
                disabled={retrying}
              >
                {retrying ? '重新生成中...' : '🔄 重新生成'}
              </button>
              <button onClick={() => navigate(-1)} className="btn btn-secondary">
                返回
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  const scenes = episode.scenes || []
  const currentSceneData = scenes[currentScene]

  return (
    <div className="episode-viewer-page">
      <audio ref={audioRef} preload="auto" autoPlay muted={false} />
      <div 
        className={`viewer-container ${isFullscreen ? 'fullscreen-mode' : ''}`} 
        ref={viewerContainerRef}
        onMouseMove={handleMouseMove}
      >
        {!isFullscreen && <div className="viewer-header">
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
        </div>}

        {scenes.length === 0 ? (
          <div className="no-scenes">
            <p>此集还没有场景内容</p>
          </div>
        ) : (
          <>
            <div className="scene-viewer">
              <div className="scene-image-container">
                <button
                  onClick={handlePrevScene}
                  className={`btn-arrow btn-arrow-left ${isFullscreen && !showControls ? 'hidden' : ''}`}
                  disabled={currentScene === 0}
                  aria-label="上一个场景"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
                  </svg>
                </button>
                
                <div className="scene-image">
                  {currentSceneData?.imageUrl ? (
                    <img key={currentScene} src={currentSceneData.imageUrl} alt={`场景 ${currentScene + 1}`} />
                  ) : (
                    <div className="scene-placeholder">
                      <span>🎬</span>
                      <p>场景 {currentScene + 1}</p>
                    </div>
                  )}
                </div>
                
                <button
                  onClick={handleNextScene}
                  className={`btn-arrow btn-arrow-right ${isFullscreen && !showControls ? 'hidden' : ''}`}
                  disabled={currentScene === scenes.length - 1}
                  aria-label="下一个场景"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
                  </svg>
                </button>
                
                <button
                  onClick={togglePlayPause}
                  className={`btn-play-pause ${isFullscreen && !showControls ? 'hidden' : ''}`}
                  aria-label={isPlaying ? "暂停" : "播放"}
                >
                  {isPlaying ? (
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"/>
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M8 5v14l11-7z"/>
                    </svg>
                  )}
                </button>

                <button
                  onClick={toggleFullscreen}
                  className={`btn-fullscreen ${isFullscreen && !showControls ? 'hidden' : ''}`}
                  aria-label={isFullscreen ? "退出全屏" : "进入全屏"}
                >
                  {isFullscreen ? (
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M5 16h3v3h2v-5H5v2zm3-8H5v2h5V5H8v3zm6 11h2v-3h3v-2h-5v5zm2-11V5h-2v5h5V8h-3z"/>
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/>
                    </svg>
                  )}
                </button>
              </div>
              
              {!isFullscreen && <div className="scene-controls-row">
                <div className="playback-controls">
                  <label className="autoplay-toggle">
                    <input
                      type="checkbox"
                      checked={autoPlay}
                      onChange={(e) => setAutoPlay(e.target.checked)}
                    />
                    <span>自动播放</span>
                  </label>
                  <p className="playback-tip">
                    {autoPlay ? '✓ 音频结束后自动切换到下一场景' : '音频结束后需手动切换场景'}
                  </p>
                </div>
              </div>}
              
              {isFullscreen && <div className={`fullscreen-controls ${!showControls ? 'hidden' : ''}`}>
                <label className="autoplay-toggle">
                  <input
                    type="checkbox"
                    checked={autoPlay}
                    onChange={(e) => setAutoPlay(e.target.checked)}
                  />
                  <span>自动播放</span>
                </label>
              </div>}
              
              {!isFullscreen && <div className="scene-text">
                <div className="scene-number">
                  场景 {currentScene + 1} / {scenes.length}
                </div>
                <p className="scene-content">{currentSceneData?.text}</p>
              </div>}
            </div>

            {!isFullscreen && <div className="viewer-controls">
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
            </div>}

            {!isFullscreen && <div className="scene-thumbnails">
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
            </div>}
          </>
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

export default EpisodeViewer
