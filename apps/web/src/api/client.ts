import type {
  Application,
  ApplicationPayload,
  ApplicationStatus,
  Company,
  Insights,
  Task,
  User
} from "./types";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

type RequestOptions = RequestInit & {
  token?: string | null;
};

type ErrorResponse = {
  message?: string;
  errors?: Record<string, string>;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");

  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const data = (await response.json()) as unknown;

  if (!response.ok) {
    throw new Error(getErrorMessage(data));
  }

  return data as T;
}

function getErrorMessage(data: unknown) {
  if (!isErrorResponse(data)) {
    return "Request failed";
  }

  if (data.errors) {
    const firstFieldError = Object.values(data.errors)[0];

    if (firstFieldError) {
      return firstFieldError;
    }
  }

  return data.message ?? "Request failed";
}

function isErrorResponse(data: unknown): data is ErrorResponse {
  return typeof data === "object" && data !== null;
}

export const api = {
  register(input: { name: string; email: string; password: string }) {
    return request<{ token: string; user: User }>("/auth/register", {
      method: "POST",
      body: JSON.stringify(input)
    });
  },

  login(input: { email: string; password: string }) {
    return request<{ token: string; user: User }>("/auth/login", {
      method: "POST",
      body: JSON.stringify(input)
    });
  },

  me(token: string) {
    return request<{ user: User }>("/auth/me", { token });
  },

  insights(token: string) {
    return request<Insights>("/applications/insights", { token });
  },

  applications(token: string, params?: { status?: ApplicationStatus; search?: string }) {
    const query = new URLSearchParams();

    if (params?.status) {
      query.set("status", params.status);
    }

    if (params?.search) {
      query.set("search", params.search);
    }

    return request<{ applications: Application[] }>(
      `/applications${query.toString() ? `?${query.toString()}` : ""}`,
      { token }
    );
  },

  createApplication(token: string, input: ApplicationPayload) {
    return request<{ application: Application }>("/applications", {
      method: "POST",
      token,
      body: JSON.stringify(input)
    });
  },

  updateApplication(token: string, id: string, input: Partial<ApplicationPayload>) {
    return request<{ application: Application }>(`/applications/${id}`, {
      method: "PATCH",
      token,
      body: JSON.stringify(input)
    });
  },

  deleteApplication(token: string, id: string) {
    return request<void>(`/applications/${id}`, {
      method: "DELETE",
      token
    });
  },

  companies(token: string) {
    return request<{ companies: Company[] }>("/companies", { token });
  },

  createTask(token: string, applicationId: string, input: { title: string; dueDate?: string | null }) {
    return request<{ task: Task }>(`/applications/${applicationId}/tasks`, {
      method: "POST",
      token,
      body: JSON.stringify(input)
    });
  },

  updateTask(token: string, taskId: string, input: Partial<Task>) {
    return request<{ task: Task }>(`/tasks/${taskId}`, {
      method: "PATCH",
      token,
      body: JSON.stringify(input)
    });
  }
};