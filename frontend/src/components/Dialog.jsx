import { useEffect } from 'react';
import Icon from './Icon';

const Dialog = ({ title, icon, children, footer, onClose, wide = false }) => {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') onClose?.();
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="modal-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose?.();
      }}
    >
      <div
        className={`dialog${wide ? ' dialog--wide' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
      >
        <div className="retro-titlebar">
          <span className="titlebar-icon">
            <Icon name={icon || 'info'} size={14} />
          </span>
          <span className="titlebar-title">{title}</span>
          {onClose ? (
            <div className="window-controls">
              <button
                type="button"
                className="window-control window-control--close"
                onClick={onClose}
                aria-label="Закрыть"
              >
                ✕
              </button>
            </div>
          ) : null}
        </div>
        <div className="dialog__body">{children}</div>
        {footer ? <div className="dialog__actions">{footer}</div> : null}
      </div>
    </div>
  );
};

export const ConfirmDialog = ({
  title = 'Подтверждение',
  message,
  confirmLabel = 'Подтвердить',
  cancelLabel = 'Отмена',
  danger = false,
  busy = false,
  onConfirm,
  onCancel,
}) => (
  <Dialog
    title={title}
    icon={danger ? 'warning' : 'info'}
    onClose={busy ? undefined : onCancel}
    footer={
      <>
        <button type="button" className="retro-btn" onClick={onCancel} disabled={busy}>
          {cancelLabel}
        </button>
        <button
          type="button"
          className={`retro-btn ${danger ? 'danger' : 'primary'}`}
          onClick={onConfirm}
          disabled={busy}
        >
          {busy ? 'Выполняется…' : confirmLabel}
        </button>
      </>
    }
  >
    <p className="dialog__message">{message}</p>
  </Dialog>
);

export default Dialog;
