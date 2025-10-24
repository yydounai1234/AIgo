import { useState } from 'react'
import TextInput from './components/TextInput'
import CharacterLibrary from './components/CharacterLibrary'
import SceneGenerator from './components/SceneGenerator'
import VoiceControls from './components/VoiceControls'
import MusicControls from './components/MusicControls'
import ExportPanel from './components/ExportPanel'
import './App.css'

function App() {
  const [novelText, setNovelText] = useState('')
  const [characters, setCharacters] = useState([])
  const [scenes, setScenes] = useState([])
  const [voiceSettings, setVoiceSettings] = useState({
    voiceType: 'female',
    speed: 1.0
  })
  const [musicSettings, setMusicSettings] = useState({
    enabled: true,
    volume: 0.5,
    type: 'background'
  })

  const handleTextSubmit = (text) => {
    setNovelText(text)
    processNovelText(text)
  }

  const processNovelText = (text) => {
    const paragraphs = text.split('\n').filter(p => p.trim().length > 0)
    
    const extractedCharacters = extractCharacters(text)
    setCharacters(extractedCharacters)
    
    const generatedScenes = paragraphs.map((paragraph, index) => ({
      id: index,
      text: paragraph,
      imageUrl: null,
      characters: extractedCharacters.filter(char => paragraph.includes(char.name)),
      audioUrl: null
    }))
    
    setScenes(generatedScenes)
  }

  const extractCharacters = (text) => {
    const characterNames = new Set()
    const namePattern = /[A-Z][a-z]+|[\u4e00-\u9fa5]{2,4}/g
    const matches = text.match(namePattern) || []
    
    matches.forEach(name => {
      if (name.length >= 2) {
        characterNames.add(name)
      }
    })
    
    return Array.from(characterNames).slice(0, 10).map((name, index) => ({
      id: index,
      name: name,
      appearance: {
        style: 'anime',
        gender: 'unknown',
        age: 'young'
      },
      imageUrl: null
    }))
  }

  const updateCharacter = (characterId, updates) => {
    setCharacters(prev => 
      prev.map(char => 
        char.id === characterId ? { ...char, ...updates } : char
      )
    )
  }

  const generateSceneImage = async (sceneId) => {
    setScenes(prev => 
      prev.map(scene => 
        scene.id === sceneId 
          ? { ...scene, imageUrl: `https://via.placeholder.com/800x450?text=Scene+${sceneId + 1}` }
          : scene
      )
    )
  }

  const generateSceneAudio = async (sceneId) => {
    setScenes(prev => 
      prev.map(scene => 
        scene.id === sceneId 
          ? { ...scene, audioUrl: 'mock-audio-url' }
          : scene
      )
    )
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>AIgo - 智能动漫生成系统</h1>
        <p>将小说文本转换为精美动漫作品</p>
      </header>

      <main className="app-main">
        <section className="input-section">
          <TextInput onSubmit={handleTextSubmit} />
        </section>

        {characters.length > 0 && (
          <section className="character-section">
            <CharacterLibrary 
              characters={characters}
              onUpdateCharacter={updateCharacter}
            />
          </section>
        )}

        {scenes.length > 0 && (
          <>
            <section className="scene-section">
              <SceneGenerator 
                scenes={scenes}
                onGenerateImage={generateSceneImage}
                onGenerateAudio={generateSceneAudio}
              />
            </section>

            <section className="controls-section">
              <VoiceControls 
                settings={voiceSettings}
                onChange={setVoiceSettings}
              />
              <MusicControls 
                settings={musicSettings}
                onChange={setMusicSettings}
              />
            </section>

            <section className="export-section">
              <ExportPanel 
                scenes={scenes}
                characters={characters}
                voiceSettings={voiceSettings}
                musicSettings={musicSettings}
              />
            </section>
          </>
        )}
      </main>
    </div>
  )
}

export default App
