import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthView } from "./AuthView";
import { useAuth } from "./AuthContext";

vi.mock("./AuthContext", () => ({
  useAuth: vi.fn()
}));

const authMock = {
  token: null,
  user: null,
  isLoading: false,
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
};

describe("AuthView", () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReturnValue(authMock);
    vi.clearAllMocks();
  });

  it("submits the sign-in form with the entered credentials", async () => {
    const user = userEvent.setup();

    render(<AuthView />);

    await user.clear(screen.getByLabelText("Email"));
    await user.type(screen.getByLabelText("Email"), "apoorva@example.com");
    await user.clear(screen.getByLabelText("Password"));
    await user.type(screen.getByLabelText("Password"), "SecretPassword123!");
    await user.click(screen.getAllByRole("button", { name: "Sign in" })[1]);

    await waitFor(() => {
      expect(authMock.login).toHaveBeenCalledWith(
        "apoorva@example.com",
        "SecretPassword123!"
      );
    });
    expect(authMock.register).not.toHaveBeenCalled();
  });

  it("switches to account creation and submits registration details", async () => {
    const user = userEvent.setup();

    render(<AuthView />);

    await user.click(screen.getAllByRole("button", { name: "Create account" })[0]);
    await user.clear(screen.getByLabelText("Name"));
    await user.type(screen.getByLabelText("Name"), "Apoorva");
    await user.clear(screen.getByLabelText("Email"));
    await user.type(screen.getByLabelText("Email"), "apoorva@example.com");
    await user.clear(screen.getByLabelText("Password"));
    await user.type(screen.getByLabelText("Password"), "SecretPassword123!");
    await user.click(screen.getAllByRole("button", { name: "Create account" })[1]);

    await waitFor(() => {
      expect(authMock.register).toHaveBeenCalledWith(
        "Apoorva",
        "apoorva@example.com",
        "SecretPassword123!"
      );
    });
    expect(authMock.login).not.toHaveBeenCalled();
  });

  it("shows authentication errors from failed submissions", async () => {
    const user = userEvent.setup();
    authMock.login.mockRejectedValueOnce(new Error("Invalid credentials"));

    render(<AuthView />);

    await user.click(screen.getAllByRole("button", { name: "Sign in" })[1]);

    expect(await screen.findByText("Invalid credentials")).toBeInTheDocument();
  });
});
