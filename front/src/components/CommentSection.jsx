import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './CommentSection.css'

function CommentSection({ targetType, targetId }) {
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [commentText, setCommentText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const isTypingRef = useRef(false)
  const typingTimeoutRef = useRef(null)

  useEffect(() => {
    loadComments()
    
    const refreshInterval = setInterval(() => {
      if (!isTypingRef.current && !submitting) {
        loadComments(true)
      }
    }, 10000)

    return () => {
      clearInterval(refreshInterval)
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current)
      }
    }
  }, [targetType, targetId, submitting])

  const loadComments = async (silent = false) => {
    if (!silent) {
      setLoading(true)
    }
    setError('')
    
    try {
      const result = await api.getComments(targetType, targetId)
      
      if (result.success) {
        setComments(result.data || [])
      } else {
        setError(result.error?.message || '加载评论失败')
      }
    } catch (err) {
      setError('加载评论时发生错误')
    } finally {
      if (!silent) {
        setLoading(false)
      }
    }
  }

  const handleSubmitComment = async () => {
    if (!isAuthenticated()) {
      navigate('/login')
      return
    }

    if (!commentText.trim()) {
      return
    }

    setSubmitting(true)
    const content = commentText.trim()
    setCommentText('')

    try {
      const result = await api.createComment(targetType, targetId, content)
      
      if (result.success && result.data) {
        setComments(prevComments => [result.data, ...prevComments])
      } else {
        setError(result.error?.message || '发表评论失败')
        setCommentText(content)
      }
    } catch (err) {
      setError('发表评论时发生错误')
      setCommentText(content)
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    if (!confirm('确定要删除这条评论吗?')) {
      return
    }

    const previousComments = comments
    setComments(prevComments => prevComments.filter(c => c.id !== commentId))

    try {
      const result = await api.deleteComment(commentId)
      
      if (!result.success) {
        setComments(previousComments)
        setError(result.error?.message || '删除评论失败')
      }
    } catch (err) {
      setComments(previousComments)
      setError('删除评论时发生错误')
    }
  }

  const formatTime = (timestamp) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    
    return date.toLocaleDateString('zh-CN')
  }

  return (
    <div className="comment-section">
      <h3 className="comment-section-title">
        评论 
        <span className="comment-count">({comments.length})</span>
      </h3>

      {isAuthenticated() && (
        <div className="comment-input-area">
          <textarea
            className="comment-textarea"
            placeholder="写下你的评论..."
            value={commentText}
            onChange={(e) => {
              setCommentText(e.target.value)
              isTypingRef.current = true
              
              if (typingTimeoutRef.current) {
                clearTimeout(typingTimeoutRef.current)
              }
              
              typingTimeoutRef.current = setTimeout(() => {
                isTypingRef.current = false
              }, 1000)
            }}
            disabled={submitting}
          />
          <div className="comment-submit-row">
            <button
              className="btn-comment btn-comment-secondary"
              onClick={() => setCommentText('')}
              disabled={submitting || !commentText.trim()}
            >
              清空
            </button>
            <button
              className="btn-comment btn-comment-primary"
              onClick={handleSubmitComment}
              disabled={submitting || !commentText.trim()}
            >
              {submitting ? '发表中...' : '发表评论'}
            </button>
          </div>
        </div>
      )}

      {!isAuthenticated() && (
        <div className="comment-input-area">
          <p style={{ color: '#666', fontSize: '14px' }}>
            <a href="/login" style={{ color: '#4a90e2', textDecoration: 'none' }}>登录</a> 后发表评论
          </p>
        </div>
      )}

      {error && (
        <div className="error-comments">{error}</div>
      )}

      {loading ? (
        <div className="loading-comments">加载评论中...</div>
      ) : comments.length === 0 ? (
        <div className="empty-comments">
          <div className="empty-comments-icon">💬</div>
          <p>还没有评论，来发表第一条评论吧！</p>
        </div>
      ) : (
<div className="comments-list">
          {comments.map((comment) => (
            <div key={comment.id} className="comment-item">
              {comment.avatarUrl ? (
                <img src={comment.avatarUrl} alt={comment.username} className="comment-avatar" />
              ) : (
                <div className="comment-avatar-placeholder">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="2"/>
                    <path d="M4 20C4 16.6863 6.68629 14 10 14H14C17.3137 14 20 16.6863 20 20" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                </div>
              )}
              <div className="comment-body">
                <div className="comment-header">
                  <span className="comment-author">{comment.username}</span>
                  <span className="comment-time">{formatTime(comment.createdAt)}</span>
                </div>
                <div className="comment-content">{comment.content}</div>
                {user && user.id === comment.userId && (
                  <div className="comment-actions">
                    <button
                      className="btn-comment-action btn-comment-delete"
                      onClick={() => handleDeleteComment(comment.id)}
                    >
                      删除
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default CommentSection
