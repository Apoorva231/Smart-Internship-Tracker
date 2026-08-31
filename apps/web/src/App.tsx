import { AuthProvider, useAuth } from "./features/auth/AuthContext";
import { AuthView } from "./features/auth/AuthView";

function AppContent() {
  const { token, isLoading, user, logout } = useAuth();

  if (isLoading) {
    return (
      <main className="loading-screen">
        <span>Loading workspace</span>
      </main>
    );
  }

  if (!token) {
    return <AuthView />;
  }

  return (
    <main className="signed-in-shell">
      <h1>Smart Internship Tracker</h1>
      <p>Signed in as {user?.name ?? "Unknown user"}.</p>
      <button type="button" onClick={logout}>
        Log out
      </button>
    </main>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
