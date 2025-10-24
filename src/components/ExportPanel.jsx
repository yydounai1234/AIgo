import { useState } from 'react'
import './ExportPanel.css'

function ExportPanel({ scenes, characters, voiceSettings }) {
  const [exporting, setExporting] = useState(false)
  const [exportFormat, setExportFormat] = useState('video')

  const handleExport = async () => {
    setExporting(true)
    
    const exportData = {
      scenes,
      characters,
      voiceSettings,
      format: exportFormat,
      timestamp: new Date().toISOString()
    }
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `anime-project-${Date.now()}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    setExporting(false)
  }

  return (
    <div className="export-panel">
      <h2>导出作品</h2>
      <p className="export-desc">将生成的动漫作品导出为可分享的格式</p>
      
      <div className="export-options">
        <div className="control-group">
          <label htmlFor="exportFormat">导出格式:</label>
          <select 
            id="exportFormat"
            value={exportFormat} 
            onChange={(e) => setExportFormat(e.target.value)}
            className="control-select"
          >
            <option value="video">视频文件 (MP4)</option>
            <option value="gif">动图 (GIF)</option>
            <option value="images">图片序列 (ZIP)</option>
            <option value="json">项目文件 (JSON)</option>
          </select>
        </div>

        <div className="export-stats">
          <div className="stat-item">
            <span className="stat-label">场景数量:</span>
            <span className="stat-value">{scenes.length}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">角色数量:</span>
            <span className="stat-value">{characters.length}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">已生成图片:</span>
            <span className="stat-value">
              {scenes.filter(s => s.imageUrl).length} / {scenes.length}
            </span>
          </div>
          <div className="stat-item">
            <span className="stat-label">已生成配音:</span>
            <span className="stat-value">
              {scenes.filter(s => s.audioUrl).length} / {scenes.length}
            </span>
          </div>
        </div>

        <button 
          onClick={handleExport}
          disabled={exporting || scenes.length === 0}
          className="btn btn-large btn-success"
        >
          {exporting ? '导出中...' : '导出作品'}
        </button>
      </div>
    </div>
  )
}

export default ExportPanel
