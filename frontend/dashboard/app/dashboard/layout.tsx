"use client";

import { ReactNode, useEffect } from "react";
import { useRouter } from "next/navigation";
import { requireAuth, logout } from "../../lib/auth";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  const router = useRouter();
  useEffect(() => {
    const token = requireAuth();
    if (!token) router.push("/login");
  }, [router]);

  return (
    <div className="min-h-screen flex flex-col">
      <header className="flex items-center justify-between px-6 py-3 bg-slate-900 border-b border-slate-800">
        <span className="font-semibold">API Monitoring Dashboard</span>
        <button onClick={() => { logout(); router.push("/login"); }} className="text-sm text-red-300">
          Logout
        </button>
      </header>
      <main className="flex-1 p-6">{children}</main>
    </div>
  );
}
