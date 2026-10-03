import { useState } from 'react';
import SearchBar from './components/SearchBar';
import StatusFilter from './components/StatusFilter';
import TaskTable from './components/TaskTable';
import { useTasks } from './hooks/useTasks';
import { useDebouncedValue } from './hooks/useDebouncedValue';

const PAGE_SIZE = 10;

export default function App() {
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  // Wait for the user to stop typing before querying, instead of one request per keystroke
  const debouncedQuery = useDebouncedValue(query.trim(), 300);

  // A new search starts from the first page; otherwise you can be left on e.g. page 4
  // of a result set that now only has one page. Resetting when the *debounced* value
  // changes (during render, not in an effect) avoids an extra request for the old term.
  const [lastQuery, setLastQuery] = useState(debouncedQuery);
  if (debouncedQuery !== lastQuery) {
    setLastQuery(debouncedQuery);
    setPage(1);
  }

  const { tasks, total, loading, error } = useTasks(debouncedQuery, status, page, PAGE_SIZE);

  const totalPages = Math.ceil(total / PAGE_SIZE);
  const firstShown = total === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const lastShown = Math.min(page * PAGE_SIZE, total);

  const handleStatusChange = (value) => {
    setStatus(value);
    setPage(1);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Task Tracker</h1>
        <p className="subtitle">Internal task management</p>
      </header>

      <div className="controls">
        <SearchBar value={query} onChange={setQuery} />
        <StatusFilter value={status} onChange={handleStatusChange} />
      </div>

      <p className="result-summary" aria-live="polite">
        {!error && total > 0 && `Showing ${firstShown}–${lastShown} of ${total} tasks`}
      </p>

      <TaskTable tasks={tasks} loading={loading} error={error} />

      {totalPages > 1 && (
        <nav className="pagination" aria-label="Pagination">
          <button disabled={page <= 1 || loading} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page} of {totalPages}
          </span>
          <button disabled={page >= totalPages || loading} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </nav>
      )}
    </div>
  );
}
