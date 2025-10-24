import './MusicControls.css'

function MusicControls({ settings, onChange }) {
  const handleEnabledChange = (e) => {
    onChange({ ...settings, enabled: e.target.checked })
  }

  const handleVolumeChange = (e) => {
    onChange({ ...settings, volume: parseFloat(e.target.value) })
  }

  const handleTypeChange = (e) => {
    onChange({ ...settings, type: e.target.value })
  }

  return (
    <div className="music-controls">
      <h3>背景音乐</h3>
      
      <div className="control-group">
        <label>
          <input 
            type="checkbox" 
            checked={settings.enabled}
            onChange={handleEnabledChange}
          />
          启用背景音乐
        </label>
      </div>

      {settings.enabled && (
        <>
          <div className="control-group">
            <label htmlFor="musicType">音乐类型:</label>
            <select 
              id="musicType"
              value={settings.type} 
              onChange={handleTypeChange}
              className="control-select"
            >
              <option value="background">轻音乐</option>
              <option value="epic">史诗</option>
              <option value="romantic">浪漫</option>
              <option value="suspense">悬疑</option>
              <option value="action">动作</option>
            </select>
          </div>

          <div className="control-group">
            <label htmlFor="volume">音量: {Math.round(settings.volume * 100)}%</label>
            <input 
              type="range" 
              id="volume"
              min="0" 
              max="1" 
              step="0.01" 
              value={settings.volume}
              onChange={handleVolumeChange}
              className="control-slider"
            />
            <div className="control-labels">
              <span>静音</span>
              <span>最大</span>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default MusicControls
