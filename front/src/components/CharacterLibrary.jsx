import { useState } from 'react'
import './CharacterLibrary.css'

function CharacterLibrary({ characters, onUpdateCharacter }) {
  const [selectedCharacter, setSelectedCharacter] = useState(null)

  const handleCharacterClick = (character) => {
    setSelectedCharacter(character.id === selectedCharacter ? null : character.id)
  }

  const handleStyleChange = (characterId, style) => {
    onUpdateCharacter(characterId, { 
      appearance: { 
        ...characters.find(c => c.id === characterId).appearance, 
        style 
      } 
    })
  }

  const handleGenderChange = (characterId, gender) => {
    onUpdateCharacter(characterId, { 
      appearance: { 
        ...characters.find(c => c.id === characterId).appearance, 
        gender 
      } 
    })
  }

  return (
    <div className="character-library">
      <h2>角色库管理</h2>
      <p className="character-library-desc">确保角色在整个故事中保持视觉一致性</p>
      
      <div className="character-grid">
        {characters.map(character => (
          <div 
            key={character.id} 
            className={`character-card ${selectedCharacter === character.id ? 'selected' : ''}`}
            onClick={() => handleCharacterClick(character)}
          >
            <div className="character-avatar">
              {character.imageUrl ? (
                <img src={character.imageUrl} alt={character.name} />
              ) : (
                <div className="character-placeholder">
                  {character.name.charAt(0)}
                </div>
              )}
            </div>
            <div className="character-info">
              <h3>{character.name}</h3>
              <span className="character-style">{character.appearance.style}</span>
            </div>
            
            {selectedCharacter === character.id && (
              <div className="character-controls">
                <div className="control-group">
                  <label>画风:</label>
                  <select 
                    value={character.appearance.style}
                    onChange={(e) => handleStyleChange(character.id, e.target.value)}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <option value="anime">动漫风</option>
                    <option value="realistic">写实风</option>
                    <option value="cartoon">卡通风</option>
                    <option value="watercolor">水彩风</option>
                  </select>
                </div>
                <div className="control-group">
                  <label>性别:</label>
                  <select 
                    value={character.appearance.gender}
                    onChange={(e) => handleGenderChange(character.id, e.target.value)}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <option value="unknown">未设定</option>
                    <option value="male">男性</option>
                    <option value="female">女性</option>
                  </select>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

export default CharacterLibrary
