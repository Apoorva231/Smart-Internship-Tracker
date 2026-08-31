import { type ReactNode, useCallback, useEffect, useState } from "react";
import { BriefcaseBusiness, CalendarDays, LogOut, Search, Target, Trophy } from "lucide-react";
import { api } from "../api/client";
import type {
  Application,
  ApplicationPayload,
  ApplicationStatus,
  Company,
  Insights,
  Task
} from "../api/types";
import { useAuth } from "../features/auth/AuthContext";
import { ApplicationForm, statusLabel } from "./ApplicationForm";
import { ApplicationList } from "./ApplicationList";
import { UpcomingTasks } from "./UpcomingTasks";

const statusFilters: Array<ApplicationStatus | "ALL"> = [
  "ALL",
  "SAVED",
  "APPLIED",
  "INTERVIEW",
  "TECHNICAL",
  "OFFER",
  "REJECTED"
];

export function Dashboard() {
  const { token, user, logout } = useAuth();
  const [applications, setApplications] = useState<Application[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [insights, setInsights] = useState<Insights | null>(null);
  const [status, setStatus] = useState<ApplicationStatus | "ALL">("ALL");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  const loadWorkspace = useCallback(async () => {
    if (!token) {
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const [applicationResult, companyResult, insightResult] = await Promise.all([
        api.applications(token, {
          status: status === "ALL" ? undefined : status,
          search: search || undefined
        }),
        api.companies(token),
        api.insights(token)
      ]);

      setApplications(applicationResult.applications);
      setCompanies(companyResult.companies);
      setInsights(insightResult);
    } catch (error) {
      setError(error instanceof Error ? error.message : "Could not load workspace");
    } finally {
      setIsLoading(false);
    }
  }, [search, status, token]);

  useEffect(() => {
    void loadWorkspace();
  }, [loadWorkspace]);

  async function createApplication(payload: ApplicationPayload) {
    if (!token) {
      return;
    }

    await api.createApplication(token, payload);
    await loadWorkspace();
  }

  async function updateStatus(application: Application, nextStatus: ApplicationStatus) {
    if (!token || application.status === nextStatus) {
      return;
    }

    await api.updateApplication(token, application.id, { status: nextStatus });
    await loadWorkspace();
  }

  async function deleteApplication(application: Application) {
    if (!token) {
      return;
    }

    await api.deleteApplication(token, application.id);
    await loadWorkspace();
  }

  async function addTask(application: Application, title: string, dueDate?: string) {
    if (!token) {
      return;
    }

    await api.createTask(token, application.id, { title, dueDate });
    await loadWorkspace();
  }

  async function toggleTask(task: Task) {
    if (!token) {
      return;
    }

    await api.updateTask(token, task.id, { completed: !task.completed });
    await loadWorkspace();
  }

  return (
    <main className="dashboard-shell">
      <header className="app-header">
        <div className="brand-lockup">
          <span className="logo-mark compact">
            <BriefcaseBusiness size={20} />
          </span>
          <div>
            <p className="eyebrow">{user?.city ?? "Personal workspace"}</p>
            <h1>My Internship Tracker</h1>
          </div>
        </div>

        <div className="header-actions">
          <span>{user?.name}</span>
          <button className="icon-button" type="button" onClick={logout}>
            <LogOut size={18} />
            <span className="sr-only">Log out</span>
          </button>
        </div>
      </header>

      <section className="metric-grid" aria-label="Application metrics">
        <Metric icon={<Target size={20} />} label="Active" value={insights?.metrics.active ?? 0} />
        <Metric
          icon={<CalendarDays size={20} />}
          label="Interviews"
          value={insights?.metrics.interviews ?? 0}
        />
        <Metric icon={<Trophy size={20} />} label="Offers" value={insights?.metrics.offers ?? 0} />
        <Metric
          icon={<BriefcaseBusiness size={20} />}
          label="High priority"
          value={insights?.metrics.highPriority ?? 0}
        />
      </section>

      {insights ? <UpcomingTasks tasks={insights.upcomingTasks} onToggleTask={toggleTask} /> : null}

      {error ? <p className="form-error">{error}</p> : null}

      <section className="workspace-layout">
        <aside className="left-rail">
          <ApplicationForm companies={companies} onCreate={createApplication} />
        </aside>

        <section className="main-column">
          <div className="controls-row">
            <div className="search-box">
              <Search size={17} />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search roles, companies, locations"
              />
            </div>

            <div className="filter-tabs" aria-label="Status filter">
              {statusFilters.map((filter) => (
                <button
                  key={filter}
                  type="button"
                  className={status === filter ? "active" : ""}
                  onClick={() => setStatus(filter)}
                >
                  {filter === "ALL" ? "All" : statusLabel(filter)}
                </button>
              ))}
            </div>
          </div>

          {isLoading ? (
            <section className="dashboard-placeholder">
              <p>Refreshing tracker</p>
            </section>
          ) : (
            <ApplicationList
              applications={applications}
              onStatusChange={updateStatus}
              onDelete={deleteApplication}
              onAddTask={addTask}
              onToggleTask={toggleTask}
            />
          )}
        </section>
      </section>
    </main>
  );
}

function Metric({
  icon,
  label,
  value
}: {
  icon: ReactNode;
  label: string;
  value: number;
}) {
  return (
    <article className="metric-card">
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </article>
  );
}
