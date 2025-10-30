import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Swiper, SwiperSlide } from 'swiper/react'
import { FreeMode, Navigation, Thumbs } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/free-mode'
import 'swiper/css/navigation'
import 'swiper/css/thumbs'
import api from '../services/api'
import Modal from '../components/Modal'
import './WorkEditor.css'

function WorkEditor() {
  const { workId } = useParams()
  const navigate = useNavigate()
  
  const [work, setWork] = useState(null)
  const [episodes, setEpisodes] = useState([])
  const [characters, setCharacters] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  
  const [workTitle, setWorkTitle] = useState('')
  const [workDescription, setWorkDescription] = useState('')
  const [isPublic, setIsPublic] = useState(false)
  
  const [showEpisodeForm, setShowEpisodeForm] = useState(false)
  const [editingEpisode, setEditingEpisode] = useState(null)
  const [episodeTitle, setEpisodeTitle] = useState('')
  const [novelText, setNovelText] = useState('')
  const [isFree, setIsFree] = useState(true)
  const [coinPrice, setCoinPrice] = useState(0)
  
  const [actionLoading, setActionLoading] = useState(false)
  
  const [modal, setModal] = useState({ isOpen: false, type: 'alert', title: '', message: '', onConfirm: null })
  const [thumbsSwiper, setThumbsSwiper] = useState(null)
  

  useEffect(() => {
    loadWork()
  }, [workId])

  const loadWork = async () => {
    setLoading(true)
    setError('')
    
    try {
      const workResult = await api.getWork(workId)
      if (workResult.success) {
        setWork(workResult.data)
        setWorkTitle(workResult.data.title)
        setWorkDescription(workResult.data.description || '')
        setIsPublic(workResult.data.isPublic)
        
        const myWorksResult = await api.getMyWorks()
        if (myWorksResult.success) {
          const currentWork = myWorksResult.data.find(w => w.id === workId)
          if (currentWork) {
            setEpisodes(currentWork.episodes || [])
          }
        }
        
        const charactersResult = await api.getWorkCharacters(workId)
        if (charactersResult.success) {
          setCharacters(charactersResult.data || [])
        }
      } else {
        setError(workResult.error?.message || '加载失败')
      }
    } catch (err) {
      setError('加载作品时发生错误')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateWork = async () => {
    if (!workTitle.trim()) {
      setError('请输入作品标题')
      return
    }
    
    if (!workDescription.trim()) {
      setError('请输入作品简介')
      return
    }
    
    setActionLoading(true)
    setError('')
    
    try {
      const result = await api.updateWork(workId, {
        title: workTitle.trim(),
        description: workDescription.trim(),
        isPublic
      })
      
      if (result.success) {
        setWork(result.data)
        setModal({ isOpen: true, type: 'alert', title: '成功', message: '作品信息已更新', onConfirm: null })
      } else {
        setError(result.error?.message || '更新失败')
      }
    } catch (err) {
      setError('更新作品时发生错误')
    } finally {
      setActionLoading(false)
    }
  }

  const handleCreateEpisode = () => {
    setEditingEpisode(null)
    setEpisodeTitle('')
    setNovelText('')
    setIsFree(true)
    setCoinPrice(0)
    setShowEpisodeForm(true)
  }

  const handleEditEpisode = async (episode) => {
    if (episode.isPublished) {
      setModal({ isOpen: true, type: 'alert', title: '提示', message: '已发布的集数不可编辑', onConfirm: null })
      return
    }
    
    setActionLoading(true)
    setError('')
    
    try {
      const result = await api.getEpisode(episode.id)
      
      if (result.success) {
        const episodeData = result.data
        setEditingEpisode(episodeData)
        setEpisodeTitle(episodeData.title)
        setNovelText(episodeData.novelText || '')
        setIsFree(episodeData.isFree)
        setCoinPrice(episodeData.coinPrice || 0)
        setShowEpisodeForm(true)
      } else {
        setError(result.error?.message || '加载集数详情失败')
      }
    } catch (err) {
      setError('加载集数详情时发生错误')
    } finally {
      setActionLoading(false)
    }
  }

  const handleSaveEpisode = async () => {
    if (!episodeTitle.trim()) {
      setError('请输入集数标题')
      return
    }
    
    if (!novelText.trim()) {
      setError('请输入小说文本')
      return
    }
    
    setActionLoading(true)
    setError('')
    
    try {
      const scenes = novelText.split('\n\n').filter(t => t.trim()).map((text, idx) => ({
        id: idx + 1,
        text: text.trim(),
        imageUrl: `https://via.placeholder.com/800x450?text=Scene+${idx + 1}`
      }))
      
      if (editingEpisode) {
        const result = await api.updateEpisode(editingEpisode.id, {
          title: episodeTitle.trim(),
          novelText: novelText.trim(),
          scenes,
          isFree,
          coinPrice: isFree ? 0 : parseInt(coinPrice) || 0
        })
        
        if (result.success) {
          await loadWork()
          setShowEpisodeForm(false)
          setModal({ isOpen: true, type: 'alert', title: '成功', message: '集数已更新', onConfirm: null })
        } else {
          setError(result.error?.message || '更新失败')
        }
      } else {
        const result = await api.createEpisode(workId, {
          title: episodeTitle.trim(),
          novelText: novelText.trim(),
          scenes,
          isFree,
          coinPrice: isFree ? 0 : parseInt(coinPrice) || 0
        })
        
        if (result.success) {
          await loadWork()
          setShowEpisodeForm(false)
          navigate(`/episode/${result.data.id}`)
        } else {
          setError(result.error?.message || '创建失败')
        }
      }
    } catch (err) {
      setError('保存集数时发生错误')
    } finally {
      setActionLoading(false)
    }
  }

  const handlePublishEpisode = async (episodeId) => {
    setModal({
      isOpen: true,
      type: 'confirm',
      title: '确认发布',
      message: '发布后将不可再编辑，确定要发布吗？',
      onConfirm: () => confirmPublishEpisode(episodeId)
    })
  }

  const confirmPublishEpisode = async (episodeId) => {
    setModal({ ...modal, isOpen: false })
    setActionLoading(true)
    setError('')
    
    try {
      const result = await api.publishEpisode(episodeId)
      
      if (result.success) {
        await loadWork()
        setModal({ isOpen: true, type: 'alert', title: '成功', message: '集数已发布', onConfirm: null })
      } else {
        setError(result.error?.message || '发布失败')
      }
    } catch (err) {
      setError('发布集数时发生错误')
    } finally {
      setActionLoading(false)
    }
  }

  const handleViewEpisode = (episodeId) => {
    navigate(`/episode/${episodeId}`)
  }


  if (loading) {
    return <div className="loading-page">加载中...</div>
  }

  if (error && !work) {
    return <div className="error-page">{error}</div>
  }

  return (
    <div className="work-editor-page">
      <div className="work-editor-container">
        <h1>编辑作品</h1>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="work-info-section">
          <h2>作品信息</h2>
          <div className="form-group">
            <label>作品标题 *</label>
            <input
              type="text"
              value={workTitle}
              onChange={(e) => setWorkTitle(e.target.value)}
              placeholder="作品标题"
              disabled={actionLoading}
            />
          </div>
          
          <div className="form-group">
            <label>作品简介 *</label>
            <textarea
              value={workDescription}
              onChange={(e) => setWorkDescription(e.target.value)}
              placeholder="简单介绍你的作品..."
              rows={4}
              disabled={actionLoading}
            />
          </div>
          
          <div className="form-group checkbox-group">
            <label>
              <input
                type="checkbox"
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
                disabled={actionLoading}
              />
              公开到作品广场
            </label>
          </div>
          
          <div className="button-container">
            <button
              onClick={handleUpdateWork}
              className="btn btn-primary btn-fixed-width"
              disabled={actionLoading}
            >
              {actionLoading ? '保存中...' : '修改作品信息'}
            </button>
          </div>
        </div>
        
        <div className="characters-section">
          <h2>角色库</h2>
          <p className="section-description">作品中出现的所有角色，确保角色在各集中保持一致性</p>
          {characters.length === 0 ? (
            <p className="empty-message">还没有角色信息，创建并处理集数后会自动提取角色</p>
          ) : (
            <div className="character-gallery">
              <Swiper
                spaceBetween={10}
                thumbs={{ swiper: thumbsSwiper }}
                modules={[FreeMode, Navigation, Thumbs]}
                className="character-main-swiper"
              >
                {characters.map(character => (
                  <SwiperSlide key={character.id}>
                    <div className="character-card">
                      <div className="character-card-content">
                        {character.firstImageUrl && (
                          <div className="character-image-large">
                            <img src={character.firstImageUrl} alt={character.name} />
                          </div>
                        )}
                        <div className="character-info">
                          <div className="character-header">
                            <h3>{character.name}</h3>
                            <div className="character-badges">
                              {character.isProtagonist && <span className="badge badge-primary">主角</span>}
                              {character.gender && (
                                <span className={`gender-badge ${character.gender}`}>
                                  {character.gender === 'male' ? '男' : character.gender === 'female' ? '女' : ''}
                                </span>
                              )}
                            </div>
                          </div>
                          {character.appearance && (
                            <div className="character-field">
                              <strong>外貌：</strong>{character.appearance}
                            </div>
                          )}
                          <div className="character-detailed-features">
                            {(character.hairType || character.hairColor) && (
                              <div className="character-feature-item">
                                <strong>发型/发色：</strong>
                                {[character.hairType, character.hairColor].filter(Boolean).join(' / ')}
                              </div>
                            )}
                            {character.faceShape && (
                              <div className="character-feature-item">
                                <strong>脸型：</strong>{character.faceShape}
                              </div>
                            )}
                            {(character.eyeType || character.eyeColor) && (
                              <div className="character-feature-item">
                                <strong>眼睛：</strong>
                                {[character.eyeType, character.eyeColor].filter(Boolean).join(' / ')}
                              </div>
                            )}
                            {character.noseType && (
                              <div className="character-feature-item">
                                <strong>鼻子：</strong>{character.noseType}
                              </div>
                            )}
                            {character.mouthType && (
                              <div className="character-feature-item">
                                <strong>嘴型：</strong>{character.mouthType}
                              </div>
                            )}
                            {character.skinTone && (
                              <div className="character-feature-item">
                                <strong>肤色：</strong>{character.skinTone}
                              </div>
                            )}
                            {(character.height || character.build) && (
                              <div className="character-feature-item">
                                <strong>身高/体型：</strong>
                                {[character.height, character.build].filter(Boolean).join(' / ')}
                              </div>
                            )}
                          </div>
                          {character.description && (
                            <div className="character-field">
                              <strong>描述：</strong>{character.description}
                            </div>
                          )}
                          {character.personality && (
                            <div className="character-field">
                              <strong>性格：</strong>{character.personality}
                            </div>
                          )}
                          {character.nicknames && character.nicknames.length > 0 && (
                            <div className="character-field">
                              <strong>别名：</strong>
                              <div className="character-nicknames">
                                {character.nicknames.map((nickname, index) => (
                                  <span key={index} className="nickname-tag">{nickname}</span>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </SwiperSlide>
                ))}
              </Swiper>
              <Swiper
                onSwiper={setThumbsSwiper}
                spaceBetween={10}
                slidesPerView={4}
                freeMode={true}
                watchSlidesProgress={true}
                modules={[FreeMode, Navigation, Thumbs]}
                className="character-thumbs-swiper"
              >
                {characters.map(character => (
                  <SwiperSlide key={character.id}>
                    <div className="character-thumb">
                      {character.firstImageUrl && (
                        <img src={character.firstImageUrl} alt={character.name} />
                      )}
                      <span className="character-thumb-name">{character.name}</span>
                    </div>
                  </SwiperSlide>
                ))}
              </Swiper>
            </div>
          )}
        </div>
        
        <div className="episodes-section">
          <div className="section-header">
            <h2>集数管理</h2>
            <button
              onClick={handleCreateEpisode}
              className="btn btn-success"
              disabled={actionLoading || showEpisodeForm}
            >
              + 新建集数
            </button>
          </div>
          
          {showEpisodeForm && (
            <div className="episode-form-card">
              <h3>{editingEpisode ? '编辑集数' : '新建集数'}</h3>
              
              <div className="form-group">
                <label>集数标题 *</label>
                <input
                  type="text"
                  value={episodeTitle}
                  onChange={(e) => setEpisodeTitle(e.target.value)}
                  placeholder="例如：开始的冒险"
                  disabled={actionLoading}
                />
              </div>
              
              <div className="form-group">
                <label>小说文本 * (用空行分隔场景)</label>
                <textarea
                  value={novelText}
                  onChange={(e) => setNovelText(e.target.value)}
                  placeholder="输入小说文本，每个段落会生成一个场景..."
                  rows={10}
                  disabled={actionLoading}
                />
                <small>提示：使用空行（两个换行符）来分隔不同的场景</small>
              </div>
              
              <div className="form-group checkbox-group">
                <label>
                  <input
                    type="checkbox"
                    checked={isFree}
                    onChange={(e) => setIsFree(e.target.checked)}
                    disabled={actionLoading}
                  />
                  免费观看
                </label>
              </div>
              
              {!isFree && (
                <div className="form-group">
                  <label>金币价格 (100金币 = 1元)</label>
                  <input
                    type="number"
                    value={coinPrice}
                    onChange={(e) => setCoinPrice(e.target.value)}
                    min="0"
                    step="10"
                    disabled={actionLoading}
                  />
                </div>
              )}
              
              <div className="form-actions">
                <button
                  onClick={handleSaveEpisode}
                  className="btn btn-primary btn-fixed-width"
                  disabled={actionLoading}
                >
                  {actionLoading ? '保存中...' : '保存'}
                </button>
                <button
                  onClick={() => setShowEpisodeForm(false)}
                  className="btn btn-secondary btn-fixed-width"
                  disabled={actionLoading}
                >
                  取消
                </button>
              </div>
            </div>
          )}
          
          <div className="episodes-list">
            {episodes.length === 0 ? (
              <p className="empty-message">还没有创建集数，点击"新建集数"开始创作</p>
            ) : (
              episodes.map(episode => (
                <div key={episode.id} className="episode-item">
                  <div className="episode-info">
                    <h4>第{episode.episodeNumber}集：{episode.title}</h4>
                    <div className="episode-meta">
                      <span className={`badge ${episode.isPublished ? 'badge-success' : 'badge-warning'}`}>
                        {episode.isPublished ? '已发布' : '未发布'}
                      </span>
                      <span className={`badge ${episode.isFree ? 'badge-info' : 'badge-primary'}`}>
                        {episode.isFree ? '免费' : `${episode.coinPrice}金币`}
                      </span>
                      <span className="badge badge-secondary">
                        {episode.scenes?.length || 0}个场景
                      </span>
                    </div>
                  </div>
                  <div className="episode-actions">
                    <button
                      onClick={() => handleViewEpisode(episode.id)}
                      className="btn btn-sm btn-info"
                    >
                      预览
                    </button>
                    {!episode.isPublished && (
                      <>
                        <button
                          onClick={() => handleEditEpisode(episode)}
                          className="btn btn-sm btn-secondary"
                          disabled={actionLoading}
                        >
                          编辑
                        </button>
                        <button
                          onClick={() => handlePublishEpisode(episode.id)}
                          className="btn btn-sm btn-success"
                          disabled={actionLoading}
                        >
                          发布
                        </button>
                      </>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
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

export default WorkEditor
