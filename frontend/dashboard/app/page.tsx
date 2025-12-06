"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();
  useEffect(() => {
    router.push("/signup");
  }, [router]);
  return (
    <main className="page-shell">
      <div className="glass-panel hero">
        <h1>Redirecting you to Project Leap…</h1>
        <p className="muted">Opening sign-up. If nothing happens, go to /signup.</p>
      </div>
    </main>
  );
}
