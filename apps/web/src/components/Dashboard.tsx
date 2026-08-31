import { type ReactNode, useCallback, useEffect, useState } from "react";
import { BriefcaseBusiness, CalendarDays, LogOut, Target, Trophy } from "lucide-react";
import { api } from "../api/client";
import type {
  Application,
  ApplicationPayload,
  ApplicationStatus,
  Company,
  Insights
} from "../api/types";
import { useAuth } from "../features/auth/AuthContext";
import { ApplicationForm } from "./ApplicationForm";
import { ApplicationList } from "./ApplicationList";

export function Dashboard() {
  const { token, user, logout } = useAuth();
  const [applications, setApplications] = useState<Application[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [insights, setInsights] = useState<Insights | null>(null);
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
        api.applications(token),
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
  }, [token]);

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

      {error ? <p className="form-error">{error}</p> : null}

      <section className="workspace-layout">
        <aside className="left-rail">
          <ApplicationForm companies={companies} onCreate={createApplication} />
        </aside>

        <section className="main-column">
          {isLoading ? (
            <section className="dashboard-placeholder">
              <p>Refreshing tracker</p>
            </section>
          ) : (
            <ApplicationList
              applications={applications}
              onStatusChange={updateStatus}
              onDelete={deleteApplication}
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
