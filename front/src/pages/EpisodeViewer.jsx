import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import api from '../services/api'
import Modal from '../components/Modal'
import CommentSection from '../components/CommentSection'
import './EpisodeViewer.css'

function EpisodeViewer() {
  const { episodeId } = useParams()
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  
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
  const emptySceneTimerRef = useRef(null)
  const [firstScenePlayClicked, setFirstScenePlayClicked] = useState(false)

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
        const nextScene = currentScene + 1
        setCurrentScene(nextScene)
        
        setTimeout(() => {
          if (audioRef.current && episode?.scenes?.[nextScene]?.audioUrl) {
            const playPromise = audioRef.current.play()
            if (playPromise !== undefined) {
              playPromise.then(() => {
                setIsPlaying(true)
              }).catch(err => {
                console.warn('Auto-play failed for next scene:', err)
                setIsPlaying(false)
              })
            }
          }
        }, 100)
      }
    }
    
    const handleEmptyTextScene = () => {
      if (autoPlay && episode?.scenes && currentScene < episode.scenes.length - 1) {
        const nextScene = currentScene + 1
        emptySceneTimerRef.current = setTimeout(() => {
          setCurrentScene(nextScene)
          
          setTimeout(() => {
            if (audioRef.current && episode?.scenes?.[nextScene]?.audioUrl) {
              const playPromise = audioRef.current.play()
              if (playPromise !== undefined) {
                playPromise.then(() => {
                  setIsPlaying(true)
                }).catch(err => {
                  console.warn('Auto-play failed for next scene:', err)
                  setIsPlaying(false)
                })
              }
            }
          }, 100)
        }, 3000)
      }
    }
    
    const handlePlay = () => setIsPlaying(true)
    const handlePause = () => setIsPlaying(false)
    
    const currentSceneData = episode?.scenes?.[currentScene]
    
    if (audioRef.current) {
      if (currentSceneData?.audioUrl && currentSceneData?.text !== '无') {
        audioRef.current.src = currentSceneData.audioUrl
        audioRef.current.load()
      }
      
      audioRef.current.addEventListener('ended', handleAudioEnded)
      audioRef.current.addEventListener('play', handlePlay)
      audioRef.current.addEventListener('pause', handlePause)
    }
    
    if (emptySceneTimerRef.current) {
      clearTimeout(emptySceneTimerRef.current)
      emptySceneTimerRef.current = null
    }
    
    const isEmptyText = !currentSceneData?.text || currentSceneData?.text === '' || currentSceneData?.text === '无'
    const isFirstScene = currentScene === 0
    
    if (isEmptyText) {
      if (isFirstScene && !firstScenePlayClicked) {
      } else {
        handleEmptyTextScene()
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
      if (emptySceneTimerRef.current) {
        clearTimeout(emptySceneTimerRef.current)
        emptySceneTimerRef.current = null
      }
    }
  }, [currentScene, episode?.scenes, autoPlay, firstScenePlayClicked])

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
    setAutoPlay(false)
    
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
      setAutoPlay(true)
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
    
    const userBalance = user?.coinBalance || 0
    
    if (userBalance < episode.coinPrice) {
      setModal({
        isOpen: true,
        type: 'confirm',
        title: '金币不足',
        message: `您的金币余额为 ${userBalance}，需要 ${episode.coinPrice} 金币才能购买。是否前往充值？`,
        onConfirm: () => {
          navigate('/recharge')
        }
      })
      return
    }
    
    setPurchasing(true)
    
    try {
      const result = await api.purchaseEpisode(episodeId)
      
      if (result.success) {
        // 更新用户金币余额
        updateUser({
          ...user,
          coinBalance: result.data.newBalance
        })
        setModal({ 
          isOpen: true, 
          type: 'alert', 
          title: '购买成功', 
          message: `消耗 ${result.data.coinCost} 金币，剩余 ${result.data.newBalance} 金币`, 
          onConfirm: null 
        })
        await loadEpisode()
      } else {
        if (result.error?.code === 'INSUFFICIENT_COINS') {
          setModal({
            isOpen: true,
            type: 'confirm',
            title: '金币不足',
            message: result.error.message + '。是否前往充值？',
            onConfirm: () => {
              navigate('/recharge')
            }
          })
        } else {
          setModal({ 
            isOpen: true, 
            type: 'alert', 
            title: '购买失败', 
            message: result.error?.message || '购买失败', 
            onConfirm: null 
          })
        }
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
    const isEmptyText = !currentSceneData?.text || currentSceneData?.text === '' || currentSceneData?.text === '无'
    const isFirstScene = currentScene === 0
    
    if (isFirstScene && isEmptyText && !firstScenePlayClicked) {
      setFirstScenePlayClicked(true)
      if (autoPlay && episode?.scenes && currentScene < episode.scenes.length - 1) {
        const nextScene = currentScene + 1
        emptySceneTimerRef.current = setTimeout(() => {
          setCurrentScene(nextScene)
          
          setTimeout(() => {
            if (audioRef.current && episode?.scenes?.[nextScene]?.audioUrl) {
              const playPromise = audioRef.current.play()
              if (playPromise !== undefined) {
                playPromise.then(() => {
                  setIsPlaying(true)
                }).catch(err => {
                  console.warn('Auto-play failed for next scene:', err)
                  setIsPlaying(false)
                })
              }
            }
          }, 100)
        }, 3000)
      }
      return
    }
    
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
    const userBalance = user?.coinBalance || 0
    const isInsufficient = userBalance < episode.coinPrice
    
    return (
      <div className="episode-viewer-page">
        <div className="purchase-prompt">
          <div className="purchase-card">
            <div className="purchase-header">
              <svg className="lock-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M8 11V7a4 4 0 018 0v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <h2>付费内容</h2>
            </div>
            <h3>{episode.title}</h3>
            <div className="balance-display-small">
              <span className="balance-label">您的余额：</span>
              <span className={`balance-value ${isInsufficient ? 'insufficient' : ''}`}>
                {userBalance} 金币
              </span>
            </div>
            <p className="price-info">
              需要支付 <strong>{episode.coinPrice}</strong> 金币才能观看此集内容
            </p>
            <p className="exchange-info">
              (100 金币 = 1 元)
            </p>
            {isInsufficient && (
              <div className="insufficient-warning">
                <svg className="warning-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 19h20L12 2z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M12 9v4M12 17h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                金币余额不足，请先充值
              </div>
            )}
            <div className="purchase-actions">
              <button
                onClick={isInsufficient ? () => navigate('/recharge') : handlePurchase}
                className="btn btn-primary btn-large"
                disabled={purchasing}
              >
                {purchasing ? '购买中...' : isInsufficient ? '充值购买' : (
                  <>
                    购买 (
                    <svg className="coin-icon-inline" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                      <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    </svg>
                    {episode.coinPrice})
                  </>
                )}
              </button>
              <button
                onClick={() => navigate(-1)}
                className="btn btn-secondary btn-large"
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
      <audio ref={audioRef} preload="auto" muted={false} />
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
            {(episode.authorName || episode.authorAvatar) && (
              <div className="episode-author">
                {episode.authorAvatar ? (
                  <img src={episode.authorAvatar} alt={episode.authorName || '作者'} className="episode-author-avatar" />
                ) : (
                  <div className="episode-author-avatar-placeholder">👤</div>
                )}
                <span className="episode-author-name">{episode.authorName || '匿名作者'}</span>
              </div>
            )}
          </div>
          <div className="episode-meta">
            {episode.isFree ? (
              <span className="badge badge-success">免费</span>
            ) : (
              <span className="badge badge-primary">
                <svg className="coin-icon-badge" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2"/>
                  <path d="M12 6v12M9 9h4.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5H9m0 3h4.5c.83 0 1.5-.67 1.5-1.5S14.33 12 13.5 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                {episode.coinPrice}
              </span>
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
              
              {isFullscreen && <div className={`fullscreen-scene-text ${!showControls ? 'hidden' : ''}`}>
                <p className="fullscreen-scene-content">{currentSceneData?.text}</p>
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

      {!needsPurchase && episode?.status === 'COMPLETED' && (
        <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 20px' }}>
          <CommentSection targetType="EPISODE" targetId={episodeId} />
        </div>
      )}

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
