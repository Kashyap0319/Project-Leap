"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { requireAuth } from "../lib/auth";

export default function Home() {
  const router = useRouter();
  useEffect(() => {
    const token = requireAuth();
    router.replace(token ? "/dashboard" : "/login");
  }, [router]);
  return null;
}
