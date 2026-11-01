const Pagination = ({ page, totalPages, onChange, totalElements, disabled = false }) => {
  if (!totalPages || totalPages <= 1) return null;

  const go = (next) => {
    if (disabled) return;
    if (next < 0 || next >= totalPages || next === page) return;
    onChange(next);
  };

  return (
    <div className="pagination">
      <button
        type="button"
        className="retro-btn retro-btn--small"
        onClick={() => go(0)}
        disabled={disabled || page === 0}
        title="Первая страница"
      >
        |◀
      </button>
      <button
        type="button"
        className="retro-btn retro-btn--small"
        onClick={() => go(page - 1)}
        disabled={disabled || page === 0}
      >
        ◀ Назад
      </button>
      <span className="pagination__info">
        Страница {page + 1} из {totalPages}
        {Number.isFinite(totalElements) ? ` · всего ${totalElements}` : ''}
      </span>
      <button
        type="button"
        className="retro-btn retro-btn--small"
        onClick={() => go(page + 1)}
        disabled={disabled || page + 1 >= totalPages}
      >
        Вперёд ▶
      </button>
      <button
        type="button"
        className="retro-btn retro-btn--small"
        onClick={() => go(totalPages - 1)}
        disabled={disabled || page + 1 >= totalPages}
        title="Последняя страница"
      >
        ▶|
      </button>
    </div>
  );
};

export default Pagination;
