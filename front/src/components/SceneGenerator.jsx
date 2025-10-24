import { useState } from 'react'
import './SceneGenerator.css'

function SceneGenerator({ scenes, onGenerateImage, onGenerateAudio }) {
  const [generatingImage, setGeneratingImage] = useState(null)
  const [generatingAudio, setGeneratingAudio] = useState(null)

  const handleGenerateImage = async (sceneId) => {
    setGeneratingImage(sceneId)
    await onGenerateImage(sceneId)
    setGeneratingImage(null)
  }

  const handleGenerateAudio = async (sceneId) => {
    setGeneratingAudio(sceneId)
    await onGenerateAudio(sceneId)
    setGeneratingAudio(null)
  }

  return (
    <div className="scene-generator">
      <h2>场景生成</h2>
      <p className="scene-generator-desc">根据小说段落自动生成场景画面和配音</p>
      
      <div className="scene-list">
        {scenes.map(scene => (
          <div key={scene.id} className="scene-item">
            <div className="scene-header">
              <h3>场景 {scene.id + 1}</h3>
              <div className="scene-actions">
                <button 
                  onClick={() => handleGenerateImage(scene.id)}
                  disabled={generatingImage === scene.id}
                  className="btn btn-sm btn-primary"
                >
                  {generatingImage === scene.id ? '生成中...' : '生成图片'}
                </button>
                <button 
                  onClick={() => handleGenerateAudio(scene.id)}
                  disabled={generatingAudio === scene.id}
                  className="btn btn-sm btn-secondary"
                >
                  {generatingAudio === scene.id ? '生成中...' : '生成配音'}
                </button>
              </div>
            </div>
            
            <div className="scene-content">
              <div className="scene-image">
                {scene.imageUrl ? (
                  <img src={scene.imageUrl} alt={`场景 ${scene.id + 1}`} />
                ) : (
                  <div className="scene-placeholder">
                    点击"生成图片"创建场景画面
                  </div>
                )}
              </div>
              
              <div className="scene-text">
                <p>{scene.text}</p>
                {scene.characters.length > 0 && (
                  <div className="scene-characters">
                    <span>角色: </span>
                    {scene.characters.map(char => (
                      <span key={char.id} className="character-tag">{char.name}</span>
                    ))}
                  </div>
                )}
                {scene.audioUrl && (
                  <div className="scene-audio">
                    <span className="audio-indicator">🔊 配音已生成</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default SceneGenerator
