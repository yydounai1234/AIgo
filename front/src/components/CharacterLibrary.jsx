import { useState } from 'react'
import { Swiper, SwiperSlide } from 'swiper/react'
import { EffectCoverflow, Navigation } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/effect-coverflow'
import 'swiper/css/navigation'
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
      
      {characters.length === 0 ? (
        <p className="no-characters">还没有角色</p>
      ) : (
        <Swiper
          effect={'coverflow'}
          grabCursor={true}
          centeredSlides={true}
          slidesPerView={'auto'}
          coverflowEffect={{
            rotate: 50,
            stretch: 0,
            depth: 100,
            modifier: 1,
            slideShadows: true,
          }}
          navigation={true}
          modules={[EffectCoverflow, Navigation]}
          className="character-swiper"
        >
          {characters.map(character => (
            <SwiperSlide key={character.id}>
              <div 
                className={`character-card-swiper ${selectedCharacter === character.id ? 'selected' : ''}`}
                onClick={() => handleCharacterClick(character)}
              >
                <div className="character-avatar-swiper">
                  {character.firstImageUrl ? (
                    <img src={character.firstImageUrl} alt={character.name} />
                  ) : (
                    <div className="character-placeholder-swiper">
                      {character.name.charAt(0)}
                    </div>
                  )}
                </div>
                <div className="character-info-swiper">
                  <h3>{character.name}</h3>
                  <span className="character-style-swiper">{character.appearance.style}</span>
                </div>
                
                {selectedCharacter === character.id && (
                  <div className="character-controls-swiper">
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
            </SwiperSlide>
          ))}
        </Swiper>
      )}
    </div>
  )
}

export default CharacterLibrary
