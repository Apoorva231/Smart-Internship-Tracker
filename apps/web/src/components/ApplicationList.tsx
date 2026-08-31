import { CalendarClock, ExternalLink, MapPin, MoreHorizontal, Trash2 } from "lucide-react";
import type { Application, ApplicationStatus } from "../api/types";
import { modeLabel, statusLabel } from "./ApplicationForm";

type Props = {
  applications: Application[];
  onStatusChange: (application: Application, status: ApplicationStatus) => Promise<void>;
  onDelete: (application: Application) => Promise<void>;
};

const statuses: ApplicationStatus[] = [
  "SAVED",
  "APPLIED",
  "INTERVIEW",
  "TECHNICAL",
  "OFFER",
  "REJECTED",
  "ARCHIVED"
];

export function ApplicationList({ applications, onStatusChange, onDelete }: Props) {
  if (applications.length === 0) {
    return (
      <section className="empty-state">
        <MoreHorizontal size={26} />
        <h2>No applications yet.</h2>
        <p>Add a target from the form to start your tracker.</p>
      </section>
    );
  }

  return (
    <section className="application-list" aria-label="Applications">
      {applications.map((application) => (
        <article className={`application-card priority-${application.priority}`} key={application.id}>
          <div className="application-main">
            <div>
              <div className="status-line">
                <span className={`status-pill ${application.status.toLowerCase()}`}>
                  {statusLabel(application.status)}
                </span>
                <span className="work-mode">{modeLabel(application.workMode)}</span>
              </div>

              <h2>{application.role}</h2>

              <div className="company-line">
                <strong>{application.company.name}</strong>
                <span>
                  <MapPin size={14} />
                  {application.company.location}
                </span>
              </div>
            </div>

            <div className="card-actions">
              {application.jobUrl ? (
                <a className="icon-button" href={application.jobUrl} target="_blank" rel="noreferrer">
                  <ExternalLink size={17} />
                  <span className="sr-only">Open job</span>
                </a>
              ) : null}

              <button className="icon-button danger" type="button" onClick={() => onDelete(application)}>
                <Trash2 size={17} />
                <span className="sr-only">Delete application</span>
              </button>
            </div>
          </div>

          <div className="application-meta">
            <span>
              <CalendarClock size={15} />
              {application.deadline ? formatDate(application.deadline) : "No deadline"}
            </span>
            <span>Priority {application.priority}</span>
            {application.salaryRange ? <span>{application.salaryRange}</span> : null}
          </div>

          {application.notes ? <p className="notes">{application.notes}</p> : null}

          <div className="status-selector" aria-label="Application status">
            {statuses.map((status) => (
              <button
                key={status}
                type="button"
                className={application.status === status ? "active" : ""}
                onClick={() => onStatusChange(application, status)}
              >
                {statusLabel(status)}
              </button>
            ))}
          </div>
        </article>
      ))}
    </section>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-CA", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(new Date(value));
}