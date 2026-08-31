import { afterEach, describe, expect, it, vi } from "vitest";
import { api } from "./client";

describe("api client", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("adds bearer auth and application filters to list requests", async () => {
    const fetchMock = vi.fn<typeof fetch>(async () => jsonResponse({ applications: [] }));
    vi.stubGlobal("fetch", fetchMock);

    await api.applications("token_123", {
      status: "INTERVIEW",
      search: "pratt"
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/applications?status=INTERVIEW&search=pratt",
      expect.objectContaining({
        headers: expect.any(Headers)
      })
    );

    const [, options] = fetchMock.mock.calls[0] ?? [];
    const headers = options?.headers as Headers;

    expect(headers.get("Authorization")).toBe("Bearer token_123");
    expect(headers.get("Content-Type")).toBe("application/json");
  });

  it("uses validation error details when a request fails", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () =>
        jsonResponse(
          {
            message: "Validation failed",
            errors: {
              role: "Role is required"
            }
          },
          { status: 400 }
        )
      )
    );

    await expect(api.createApplication("token_123", { role: "" })).rejects.toThrow(
      "Role is required"
    );
  });
});

function jsonResponse(body: unknown, init?: ResponseInit) {
  return new Response(JSON.stringify(body), {
    headers: {
      "Content-Type": "application/json"
    },
    status: init?.status ?? 200
  });
}
