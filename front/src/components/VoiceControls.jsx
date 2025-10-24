import './VoiceControls.css'

function VoiceControls({ settings, onChange }) {
  const handleVoiceTypeChange = (e) => {
    onChange({ ...settings, voiceType: e.target.value })
  }

  const handleSpeedChange = (e) => {
    onChange({ ...settings, speed: parseFloat(e.target.value) })
  }

  return (
    <div className="voice-controls">
      <h3>配音设置</h3>
      
      <div className="control-group">
        <label htmlFor="voiceType">配音类型:</label>
        <select 
          id="voiceType"
          value={settings.voiceType} 
          onChange={handleVoiceTypeChange}
          className="control-select"
        >
          <option value="female">女声</option>
          <option value="male">男声</option>
          <option value="child">童声</option>
          <option value="elder">老年声</option>
        </select>
      </div>

      <div className="control-group">
        <label htmlFor="speed">语速: {settings.speed.toFixed(1)}x</label>
        <input 
          type="range" 
          id="speed"
          min="0.5" 
          max="2.0" 
          step="0.1" 
          value={settings.speed}
          onChange={handleSpeedChange}
          className="control-slider"
        />
        <div className="control-labels">
          <span>慢</span>
          <span>快</span>
        </div>
      </div>
    </div>
  )
}

export default VoiceControls
