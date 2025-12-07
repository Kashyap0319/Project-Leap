"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { ProtectedFrame } from "../../components/layout/app-shell";
import { requireAuth } from "../../lib/auth";

export default function LogsLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  useEffect(() => {
    const token = requireAuth();
    if (!token) router.replace("/login");
  }, [router]);
  return <ProtectedFrame>{children}</ProtectedFrame>;
}
