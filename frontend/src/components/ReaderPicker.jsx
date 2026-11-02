import { useEffect, useRef, useState } from 'react';
import { getReaders, searchReaders } from '../api/readers';
import { getErrorMessage } from '../utils/errors';
import useDebouncedValue from '../hooks/useDebouncedValue';
import Icon from './Icon';

const readerText = (reader) => {
  if (!reader) return '';
  const card = reader.libraryCardNumber ? ` · ${reader.libraryCardNumber}` : '';
  return `${reader.fullName}${card}`;
};

const ReaderPicker = ({
  value,
  onChange,
  placeholder = 'Найти читателя по имени или email…',
  disabled = false,
}) => {
  const inputRef = useRef(null);
  const [editing, setEditing] = useState(!value);
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const debouncedQuery = useDebouncedValue(query, 300);

  useEffect(() => {
    if (value) setEditing(false);
  }, [value]);

  useEffect(() => {
    if (!editing || !open) return undefined;

    let ignore = false;
    const request = debouncedQuery.trim()
      ? searchReaders(debouncedQuery.trim())
      : getReaders(0, 100);

    setLoading(true);
    setError('');

    request
      .then((response) => {
        if (ignore) return;
        const list = Array.isArray(response.data) ? response.data : response.data?.content || [];
        setOptions(list.filter((reader) => reader.roleName === 'READER' || !reader.roleName));
      })
      .catch((requestError) => {
        if (ignore) return;
        setOptions([]);
        setError(getErrorMessage(requestError, 'Не удалось загрузить читателей'));
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedQuery, editing, open]);

  const selectReader = (reader) => {
    onChange(reader);
    setEditing(false);
    setOpen(false);
    setQuery('');
  };

  const clearReader = () => {
    onChange(null);
    setQuery('');
    setEditing(true);
    setOpen(true);
    window.setTimeout(() => inputRef.current?.focus(), 0);
  };

  if (value && !editing) {
    return (
      <div className="reader-selected">
        <Icon name="user" size={14} />
        <div>
          <strong>{value.fullName}</strong>
          <div className="reader-option__meta">
            {value.libraryCardNumber || 'без номера билета'}
            {value.email ? ` · ${value.email}` : ''}
            {` · на руках: ${value.activeLoansCount ?? 0}`}
          </div>
        </div>
        <button
          type="button"
          className="retro-btn retro-btn--small reader-selected__clear"
          onClick={clearReader}
          disabled={disabled}
        >
          Изменить
        </button>
      </div>
    );
  }

  return (
    <div className="reader-picker">
      <input
        ref={inputRef}
        className="retro-input reader-picker__input"
        value={query}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => window.setTimeout(() => setOpen(false), 120)}
        placeholder={placeholder}
        disabled={disabled}
        autoComplete="off"
      />
      {open ? (
        <div className="reader-picker__menu" role="listbox">
          {loading ? <div className="reader-option__empty">Загрузка…</div> : null}
          {!loading && error ? <div className="reader-option__empty">{error}</div> : null}
          {!loading && !error && options.length === 0 ? (
            <div className="reader-option__empty">Читатели не найдены</div>
          ) : null}
          {!loading
            ? options.map((reader) => (
                <button
                  key={reader.id}
                  type="button"
                  className="reader-option"
                  onMouseDown={(event) => event.preventDefault()}
                  onClick={() => selectReader(reader)}
                >
                  <span>
                    <span className="reader-option__name">{reader.fullName}</span>
                    <span className="reader-option__meta">
                      {reader.email}
                      {reader.libraryCardNumber ? ` · ${reader.libraryCardNumber}` : ''}
                    </span>
                  </span>
                  <span className="badge neutral">{reader.activeLoansCount ?? 0} на руках</span>
                </button>
              ))
            : null}
        </div>
      ) : null}
    </div>
  );
};

export default ReaderPicker;
