import { useState } from 'react'
import './TextInput.css'

function TextInput({ onSubmit }) {
  const [text, setText] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (text.trim()) {
      onSubmit(text)
    }
  }

  const handleClear = () => {
    setText('')
  }

  return (
    <div className="text-input">
      <h2>小说文本输入</h2>
      <form onSubmit={handleSubmit}>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="请输入或粘贴您的小说文本内容..."
          rows={10}
          className="text-input-area"
        />
        <div className="text-input-actions">
          <button type="button" onClick={handleClear} className="btn btn-secondary">
            清空
          </button>
          <button type="submit" className="btn btn-primary">
            开始生成
          </button>
        </div>
      </form>
      <div className="text-input-info">
        <p>字数统计: {text.length} 字</p>
      </div>
    </div>
  )
}

export default TextInput
