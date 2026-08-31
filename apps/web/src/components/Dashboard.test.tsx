import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Application, Company, Insights, User } from "../api/types";
import { api } from "../api/client";
import { useAuth } from "../features/auth/AuthContext";
import { Dashboard } from "./Dashboard";

vi.mock("../api/client", () => ({
  api: {
    applications: vi.fn(),
    companies: vi.fn(),
    insights: vi.fn(),
    createApplication: vi.fn(),
    updateApplication: vi.fn(),
    deleteApplication: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn()
  }
}));

vi.mock("../features/auth/AuthContext", () => ({
  useAuth: vi.fn()
}));

const authUser: User = {
  id: "user_123",
  name: "Apoorva213",
  email: "apoorva@example.com",
  city: "Montreal, QC"
};

const company: Company = {
  id: "company_123",
  name: "Pratt and Whitney",
  location: "Montreal",
  website: null,
  industry: "Aerospace",
  size: null,
  createdAt: "2026-08-31T12:00:00",
  updatedAt: "2026-08-31T12:00:00"
};

const application: Application = {
  id: "app_123",
  role: "Data Engineer Intern",
  status: "INTERVIEW",
  workMode: "HYBRID",
  priority: 2,
  deadline: "2026-09-15T00:00:00",
  jobUrl: null,
  salaryRange: null,
  contactName: null,
  contactEmail: null,
  notes: "Practice SQL stories",
  appliedAt: null,
  createdAt: "2026-08-31T12:00:00",
  updatedAt: "2026-08-31T12:00:00",
  company,
  tasks: [
    {
      id: "task_123",
      title: "Email recruiter",
      dueDate: "2026-09-01T09:00:00",
      completed: false,
      createdAt: "2026-08-31T12:00:00",
      updatedAt: "2026-08-31T12:00:00"
    }
  ]
};

const insights: Insights = {
  counts: [
    {
      status: "INTERVIEW",
      _count: {
        status: 1
      }
    }
  ],
  metrics: {
    total: 1,
    active: 1,
    interviews: 1,
    offers: 0,
    highPriority: 0
  },
  upcomingTasks: [
    {
      ...application.tasks[0],
      application: {
        id: application.id,
        role: application.role,
        status: application.status,
        company
      }
    }
  ]
};

describe("Dashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useAuth).mockReturnValue({
      token: "token_123",
      user: authUser,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn()
    });
    vi.mocked(api.applications).mockResolvedValue({ applications: [application] });
    vi.mocked(api.companies).mockResolvedValue({ companies: [company] });
    vi.mocked(api.insights).mockResolvedValue(insights);
    vi.mocked(api.createApplication).mockResolvedValue({ application });
    vi.mocked(api.updateApplication).mockResolvedValue({ application });
    vi.mocked(api.deleteApplication).mockResolvedValue(undefined);
    vi.mocked(api.createTask).mockResolvedValue({ task: application.tasks[0] });
    vi.mocked(api.updateTask).mockResolvedValue({
      task: {
        ...application.tasks[0],
        completed: true
      }
    });
  });

  it("loads dashboard data for the signed-in user", async () => {
    render(<Dashboard />);

    expect(screen.getByText("Refreshing tracker")).toBeInTheDocument();

    expect(
      await screen.findByRole("heading", { name: "Data Engineer Intern" })
    ).toBeInTheDocument();
    expect(screen.getByText("Apoorva213")).toBeInTheDocument();
    expect(screen.getByText("Upcoming follow-ups")).toBeInTheDocument();
    expect(screen.getAllByText("Email recruiter")).toHaveLength(2);

    expect(api.applications).toHaveBeenCalledWith("token_123", {
      status: undefined,
      search: undefined
    });
    expect(api.companies).toHaveBeenCalledWith("token_123");
    expect(api.insights).toHaveBeenCalledWith("token_123");
  });

  it("reloads applications when search and status filters change", async () => {
    const user = userEvent.setup();

    render(<Dashboard />);
    await screen.findByRole("heading", { name: "Data Engineer Intern" });

    await user.type(screen.getByPlaceholderText("Search roles, companies, locations"), "pratt");
    await user.click(
      within(screen.getByLabelText("Status filter")).getByRole("button", { name: "Interview" })
    );

    await waitFor(() => {
      expect(api.applications).toHaveBeenCalledWith("token_123", {
        status: "INTERVIEW",
        search: "pratt"
      });
    });
  });

  it("creates applications from the dashboard form", async () => {
    const user = userEvent.setup();

    render(<Dashboard />);
    await screen.findByRole("heading", { name: "Data Engineer Intern" });

    await user.type(screen.getByLabelText("Role"), "Backend Intern");
    await user.type(screen.getByLabelText("Company name"), "Shopify");
    await user.click(screen.getByRole("button", { name: "Add application" }));

    await waitFor(() => {
      expect(api.createApplication).toHaveBeenCalledWith(
        "token_123",
        expect.objectContaining({
          role: "Backend Intern",
          companyName: "Shopify",
          companyLocation: "Remote / Hybrid / City",
          status: "SAVED",
          workMode: "HYBRID",
          priority: 2,
          deadline: null
        })
      );
    });
  });

  it("updates application status and toggles follow-up tasks", async () => {
    const user = userEvent.setup();

    render(<Dashboard />);
    await screen.findByRole("heading", { name: "Data Engineer Intern" });

    const applicationList = screen.getByLabelText("Applications");

    await user.click(within(applicationList).getByRole("button", { name: "Offer" }));
    await user.click(within(applicationList).getByRole("button", { name: "Email recruiter" }));

    expect(api.updateApplication).toHaveBeenCalledWith("token_123", "app_123", {
      status: "OFFER"
    });
    expect(api.updateTask).toHaveBeenCalledWith("token_123", "task_123", {
      completed: true
    });
  });
});
