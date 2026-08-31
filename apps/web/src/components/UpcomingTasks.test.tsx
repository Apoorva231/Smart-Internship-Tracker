import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { Insights } from "../api/types";
import { UpcomingTasks } from "./UpcomingTasks";

describe("UpcomingTasks", () => {
  it("renders upcoming follow-ups and completes them from the panel", async () => {
    const user = userEvent.setup();
    const onToggleTask = vi.fn(async () => undefined);

    render(<UpcomingTasks tasks={[upcomingTask]} onToggleTask={onToggleTask} />);

    expect(screen.getByRole("heading", { name: "Upcoming follow-ups" })).toBeInTheDocument();
    expect(screen.getByText("Follow up recruiter")).toBeInTheDocument();
    expect(screen.getByText("Pratt and Whitney")).toBeInTheDocument();
    expect(screen.getByText("Data Engineer Intern")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Mark Follow up recruiter complete" }));

    expect(onToggleTask).toHaveBeenCalledWith(upcomingTask);
  });

  it("shows an empty state when there are no dated follow-ups", () => {
    render(<UpcomingTasks tasks={[]} onToggleTask={vi.fn(async () => undefined)} />);

    expect(screen.getByText("No dated follow-ups yet.")).toBeInTheDocument();
  });
});

const upcomingTask: Insights["upcomingTasks"][number] = {
  id: "task_123",
  title: "Follow up recruiter",
  dueDate: "2026-09-01T09:00:00",
  completed: false,
  createdAt: "2026-08-31T12:00:00",
  updatedAt: "2026-08-31T12:00:00",
  application: {
    id: "app_123",
    role: "Data Engineer Intern",
    status: "INTERVIEW",
    company: {
      id: "company_123",
      name: "Pratt and Whitney",
      location: "Montreal",
      website: null,
      industry: "Aerospace",
      size: null,
      createdAt: "2026-08-31T12:00:00",
      updatedAt: "2026-08-31T12:00:00"
    }
  }
};
