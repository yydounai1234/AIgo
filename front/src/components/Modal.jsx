import './Modal.css'

function Modal({ isOpen, onClose, onConfirm, title, message, type = 'alert' }) {
  if (!isOpen) return null

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose()
    }
  }

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="modal-container">
        <div className="modal-header">
          <h3>{title}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <p>{message}</p>
        </div>
        <div className="modal-footer">
          {type === 'confirm' ? (
            <>
              <button className="btn btn-secondary" onClick={onClose}>
                取消
              </button>
              <button className="btn btn-primary" onClick={onConfirm}>
                确定
              </button>
            </>
          ) : (
            <button className="btn btn-primary" onClick={onClose}>
              确定
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

export default Modal
