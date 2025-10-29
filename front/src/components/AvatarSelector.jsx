import { useState, useRef } from 'react'
import './AvatarSelector.css'

const SYSTEM_AVATARS = [
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Jasper',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Bella',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Oliver',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Luna',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Max',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Lucy'
]

function AvatarSelector({ onConfirm }) {
  const [selectedType, setSelectedType] = useState(null)
  const [selectedAvatar, setSelectedAvatar] = useState(null)
  const [selectedAvatarUrl, setSelectedAvatarUrl] = useState(null)
  const [uploadedImage, setUploadedImage] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const fileInputRef = useRef(null)

  const handleFileSelect = (e) => {
    const file = e.target.files[0]
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setError('请选择图片文件')
      return
    }

    if (file.size > 5 * 1024 * 1024) {
      setError('图片大小不能超过5MB')
      return
    }

    setError('')
    const reader = new FileReader()
    reader.onload = (event) => {
      const base64Data = event.target.result
      setUploadedImage(base64Data)
      setSelectedAvatar(base64Data)
      setSelectedAvatarUrl(null)
      setSelectedType('upload')
    }
    reader.readAsDataURL(file)
  }

  const handleSystemAvatarSelect = async (avatarUrl) => {
    setLoading(true)
    setError('')
    
    try {
      const img = new Image()
      img.crossOrigin = 'anonymous'
      
      img.onload = () => {
        try {
          const canvas = document.createElement('canvas')
          canvas.width = 256
          canvas.height = 256
          
          const ctx = canvas.getContext('2d')
          ctx.drawImage(img, 0, 0, 256, 256)
          
          canvas.toBlob((blob) => {
            if (!blob) {
              setError('系统头像转换失败，请重试')
              setLoading(false)
              return
            }
            
            const reader = new FileReader()
            reader.onload = (event) => {
              const base64Data = event.target.result
              setSelectedAvatar(base64Data)
              setSelectedAvatarUrl(avatarUrl)
              setSelectedType('system')
              setUploadedImage(base64Data)
              setLoading(false)
            }
            reader.onerror = () => {
              setError('系统头像加载失败，请重试')
              setLoading(false)
            }
            reader.readAsDataURL(blob)
          }, 'image/png', 0.95)
        } catch (err) {
          console.error('Failed to convert SVG to PNG:', err)
          setError('系统头像转换失败，请重试')
          setLoading(false)
        }
      }
      
      img.onerror = () => {
        setError('系统头像加载失败，请重试')
        setLoading(false)
      }
      
      img.src = avatarUrl
    } catch (err) {
      console.error('Failed to load system avatar:', err)
      setError('系统头像加载失败，请重试')
      setLoading(false)
    }
  }

  const handleConfirm = () => {
    if (!selectedAvatar) {
      setError('请选择或上传一个头像')
      return
    }
    onConfirm(selectedAvatar, selectedType)
  }

  return (
    <div className="avatar-selector-overlay">
      <div className="avatar-selector-modal">
        <h2>选择头像</h2>
        
        <div className="avatar-selector-content">
          <div className="avatar-upload-section">
            <h3>上传自定义头像</h3>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleFileSelect}
              className="avatar-upload-input"
            />
            <button
              className="avatar-upload-btn"
              onClick={() => fileInputRef.current?.click()}
            >
              选择图片
            </button>
            {uploadedImage && (
              <div className="avatar-preview">
                <img src={uploadedImage} alt="上传预览" />
              </div>
            )}
          </div>

          <div className="avatar-divider">或</div>

          <div className="system-avatars-section">
            <h3>选择系统头像</h3>
            <div className="system-avatars-grid">
              {SYSTEM_AVATARS.map((avatarUrl, index) => (
                <div
                  key={index}
                  className={`system-avatar-item ${selectedType === 'system' && selectedAvatarUrl === avatarUrl ? 'selected' : ''}`}
                  onClick={() => handleSystemAvatarSelect(avatarUrl)}
                >
                  <img src={avatarUrl} alt={`系统头像 ${index + 1}`} />
                </div>
              ))}
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="avatar-selector-actions">
            <button
              className="btn-confirm"
              onClick={handleConfirm}
              disabled={!selectedAvatar || loading}
            >
              {loading ? '加载中...' : '确认'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AvatarSelector
