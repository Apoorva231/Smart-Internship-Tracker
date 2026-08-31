import { CalendarClock, CheckCircle2, ClipboardList } from "lucide-react";
import type { Insights, Task } from "../api/types";
import { statusLabel } from "./ApplicationForm";

type UpcomingTask = Insights["upcomingTasks"][number];

type Props = {
  tasks: UpcomingTask[];
  onToggleTask: (task: Task) => Promise<void>;
};

export function UpcomingTasks({ tasks, onToggleTask }: Props) {
  const datedTasks = tasks.filter((task) => task.dueDate);

  return (
    <section className="followup-panel" aria-label="Upcoming follow-ups">
      <div className="followup-header">
        <div>
          <p className="eyebrow">Next steps</p>
          <h2>Upcoming follow-ups</h2>
        </div>
        <span className="followup-count">
          <ClipboardList size={16} />
          {datedTasks.length}
        </span>
      </div>

      {datedTasks.length === 0 ? (
        <p className="followup-empty">No dated follow-ups yet.</p>
      ) : (
        <div className="followup-list">
          {datedTasks.map((task) => (
            <article className="followup-item" key={task.id}>
              <button
                className="followup-check"
                type="button"
                onClick={() => onToggleTask(task)}
                aria-label={`Mark ${task.title} complete`}
              >
                <CheckCircle2 size={18} />
              </button>

              <div className="followup-copy">
                <div className="followup-title-row">
                  <h3>{task.title}</h3>
                  <span className={`status-pill ${task.application.status.toLowerCase()}`}>
                    {statusLabel(task.application.status)}
                  </span>
                </div>

                <p>{task.application.company.name}</p>

                <div className="followup-meta">
                  <span className={`followup-due ${getDueTone(task.dueDate)}`}>
                    <CalendarClock size={14} />
                    {formatDueDate(task.dueDate)}
                  </span>
                  <span>{task.application.role}</span>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function formatDueDate(value: string | null | undefined) {
  if (!value) {
    return "No date";
  }

  return new Intl.DateTimeFormat("en-CA", {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date(value));
}

function getDueTone(value: string | null | undefined) {
  if (!value) {
    return "";
  }

  const dueDate = new Date(value);
  const now = new Date();
  const tomorrow = new Date(now);
  tomorrow.setDate(now.getDate() + 1);

  if (dueDate < now) {
    return "overdue";
  }

  if (
    dueDate.toDateString() === now.toDateString() ||
    dueDate.toDateString() === tomorrow.toDateString()
  ) {
    return "soon";
  }

  return "";
}
