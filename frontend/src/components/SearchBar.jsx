export default function SearchBar({ value, onChange }) {
  return (
    <div className="search-wrapper">
      <input
        type="text"
        className="search-input"
        placeholder="Search tasks..."
        aria-label="Search tasks"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={(e) => e.key === 'Escape' && onChange('')}
      />
      {value && (
        <button type="button" className="search-clear" aria-label="Clear search" onClick={() => onChange('')}>
          &times;
        </button>
      )}
    </div>
  );
}
