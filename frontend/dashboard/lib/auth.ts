"use client";

import axios from "axios";
import { setToken, api } from "./api";

function persistSession(username: string, token: string) {
  if (typeof window === "undefined") return;
  localStorage.setItem("leap.session", JSON.stringify({ username, token }));
}

async function authenticate(path: string, username: string, password: string): Promise<string> {
  const payload = {
    username: username.trim(),
    password: password.trim(),
  };
  if (!payload.username || !payload.password) {
    throw new Error("Username and password required");
  }
  try {
    const { data } = await api.post<{ token: string }>(path, payload);
    const token = data.token;
    setToken(token);
    persistSession(payload.username, token);
    return token;
  } catch (err) {
    if (axios.isAxiosError(err)) {
      const message = (err.response?.data as { message?: string } | undefined)?.message;
      throw new Error(message || "Authentication failed");
    }
    throw new Error("Authentication failed");
  }
}

export async function login(username: string, password: string): Promise<string> {
  return authenticate("/auth/login", username, password);
}

export async function signup(username: string, password: string): Promise<string> {
  return authenticate("/auth/signup", username, password);
}

export function logout() {
  setToken(null);
  if (typeof window !== "undefined") {
    localStorage.removeItem("token");
    localStorage.removeItem("leap.session");
  }
}

export function requireAuth() {
  if (typeof window === "undefined") return null;
  const legacy = localStorage.getItem("token");
  const sessionRaw = localStorage.getItem("leap.session");
  let token: string | null = null;
  if (sessionRaw) {
    try {
      const parsed = JSON.parse(sessionRaw);
      token = parsed.token;
    } catch {
      token = null;
    }
  } else if (legacy) {
    token = legacy;
  }
  if (token) setToken(token);
  return token;
}

export function currentUser(): string | null {
  if (typeof window === "undefined") return null;
  const sessionRaw = localStorage.getItem("leap.session");
  if (!sessionRaw) return null;
  try {
    const parsed = JSON.parse(sessionRaw);
    return parsed.username as string;
  } catch {
    return null;
  }
}

export async function fetchLogs(params: Record<string, any>) {
  const res = await api.get("/api/logs", { params });
  return res.data;
}

export async function fetchIncidents() {
  const res = await api.get("/api/incidents");
  return res.data;
}

export async function resolveIncident(id: string, version: number) {
  const res = await api.post(`/api/incidents/${id}/resolve`, { version });
  return res.data;
}
