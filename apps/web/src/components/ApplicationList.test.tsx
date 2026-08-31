import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { Application } from "../api/types";
import { ApplicationList } from "./ApplicationList";

describe("ApplicationList", () => {
  it("submits a dated follow-up task for an application", async () => {
    const user = userEvent.setup();
    const onAddTask = vi.fn(async () => undefined);

    render(
      <ApplicationList
        applications={[application]}
        onStatusChange={vi.fn(async () => undefined)}
        onDelete={vi.fn(async () => undefined)}
        onAddTask={onAddTask}
        onToggleTask={vi.fn(async () => undefined)}
      />
    );

    await user.type(screen.getByPlaceholderText("Add follow-up"), "Follow up recruiter");
    await user.type(screen.getByLabelText("Follow-up due date"), "2026-09-01");
    await user.click(screen.getByRole("button", { name: "Add task" }));

    expect(onAddTask).toHaveBeenCalledWith(
      application,
      "Follow up recruiter",
      expect.stringContaining("2026-09-01T09:00:00")
    );
    expect(screen.getByPlaceholderText("Add follow-up")).toHaveValue("");
  });

  it("calls status and task handlers from the card controls", async () => {
    const user = userEvent.setup();
    const onStatusChange = vi.fn(async () => undefined);
    const onToggleTask = vi.fn(async () => undefined);

    render(
      <ApplicationList
        applications={[application]}
        onStatusChange={onStatusChange}
        onDelete={vi.fn(async () => undefined)}
        onAddTask={vi.fn(async () => undefined)}
        onToggleTask={onToggleTask}
      />
    );

    await user.click(screen.getByRole("button", { name: "Offer" }));
    await user.click(screen.getByRole("button", { name: "Email recruiter" }));

    expect(onStatusChange).toHaveBeenCalledWith(application, "OFFER");
    expect(onToggleTask).toHaveBeenCalledWith(application.tasks[0]);
  });
});

const application: Application = {
  id: "app_123",
  role: "Data Engineer Intern",
  status: "INTERVIEW",
  workMode: "HYBRID",
  priority: 2,
  deadline: "2026-09-15T00:00:00",
  jobUrl: "https://example.com/jobs/data-engineer-intern",
  salaryRange: null,
  contactName: null,
  contactEmail: null,
  notes: "Prepare SQL stories",
  appliedAt: null,
  createdAt: "2026-08-31T12:00:00",
  updatedAt: "2026-08-31T12:00:00",
  company: {
    id: "company_123",
    name: "Pratt and Whitney",
    location: "Montreal",
    website: null,
    industry: "Aerospace",
    size: null,
    createdAt: "2026-08-31T12:00:00",
    updatedAt: "2026-08-31T12:00:00"
  },
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
