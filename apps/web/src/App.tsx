import { Dashboard } from "./components/Dashboard";
import { AuthProvider, useAuth } from "./features/auth/AuthContext";
import { AuthView } from "./features/auth/AuthView";

function AppContent() {
  const { token, isLoading } = useAuth();

  if (isLoading) {
    return (
      <main className="loading-screen">
        <span>Loading workspace</span>
      </main>
    );
  }

  return token ? <Dashboard /> : <AuthView />;
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}