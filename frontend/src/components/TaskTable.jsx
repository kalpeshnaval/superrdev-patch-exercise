import { STATUS_LABELS } from './StatusFilter';

const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' });

function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : dateFormat.format(date);
}

export default function TaskTable({ tasks, loading, error }) {
  if (error) {
    return (
      <div className="state-message error" role="alert">
        Error: {error}
      </div>
    );
  }

  const hasRows = tasks && tasks.length > 0;

  // Only show the full-page loading message on first load; afterwards keep the
  // previous rows visible (dimmed) so the table doesn't flash on every keystroke.
  if (!hasRows) {
    return <div className="state-message">{loading ? 'Loading tasks...' : 'No tasks found.'}</div>;
  }

  return (
    <div className="table-wrapper">
      <table className={`task-table${loading ? ' is-loading' : ''}`} aria-busy={loading}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Status</th>
            <th>Priority</th>
            <th>Assignee</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map((task) => (
            <tr key={task.id}>
              <td>{task.id}</td>
              <td>
                <div className="task-title">{task.title}</div>
                {task.description && <div className="task-desc">{task.description}</div>}
              </td>
              <td>
                <span className={`status-badge ${task.status.toLowerCase()}`}>
                  {STATUS_LABELS[task.status] ?? task.status}
                </span>
              </td>
              <td>{task.priority || '—'}</td>
              <td>{task.assignee || '—'}</td>
              <td className="nowrap">{formatDate(task.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
