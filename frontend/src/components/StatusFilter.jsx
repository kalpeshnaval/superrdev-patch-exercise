export const STATUS_LABELS = {
  OPEN: 'Open',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
};

export default function StatusFilter({ value, onChange }) {
  return (
    <select
      className="status-filter"
      aria-label="Filter by status"
      value={value}
      onChange={(e) => onChange(e.target.value)}
    >
      <option value="">All statuses</option>
      {Object.entries(STATUS_LABELS).map(([status, label]) => (
        <option key={status} value={status}>
          {label}
        </option>
      ))}
    </select>
  );
}
