"use client";

import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { api } from "./api";
import { clearSession, getSession, getToken, isExpired, setSession, upsertUser } from "./session";


export type AuthUser = {
  email: string;
  fullName?: string;
  avatarInitials?: string;
  city?: string;
  country?: string;
  lastLoginAt?: string;
};

type AuthResponse = {
  token: string;
  user?: AuthUser;
};

function formatError(err: unknown) {
  if (axios.isAxiosError(err)) {
    return (
      (err.response?.data as { message?: string } | undefined)?.message ||
      err.response?.statusText ||
      "Authentication failed"
    );
  }
  return err instanceof Error ? err.message : "Authentication failed";
}

async function authenticate(path: string, body: Record<string, string>): Promise<AuthResponse> {
  const clean = Object.fromEntries(Object.entries(body).map(([k, v]) => [k, v.trim()])) as Record<string, string>;
  if (!clean.email || !clean.password) {
    throw new Error("Email and password are required");
  }
  try {
    const payload = { username: clean.email, password: clean.password, fullName: clean.fullName } as Record<string, string>;
    const { data } = await api.post<AuthResponse>(path, payload);
    if (!data.token) {
      throw new Error("Token missing in response");
    }
    setSession(data.token, data.user ? { email: data.user.email, name: data.user.fullName } : { email: clean.email });
    return data;
  } catch (err) {
    throw new Error(formatError(err));
  }
}

export async function login(email: string, password: string) {
  return authenticate("/auth/login", { email, password });
}

export async function signup(fullName: string, email: string, password: string) {
  return authenticate("/auth/signup", { fullName, email, password });
}

export async function fetchMe(): Promise<AuthUser> {
  const token = getToken();
  if (!token) throw new Error("Not authenticated");
  try {
    const decoded = jwtDecode<{ sub?: string; email?: string }>(token);
    const email = decoded.email || decoded.sub;
    if (!email) throw new Error("User info missing in token");
    const user: AuthUser = { email, fullName: decoded.email };
    upsertUser({ email, name: decoded.email });
    return user;
  } catch (err) {
    throw new Error(formatError(err));
  }
}

export function requireAuth(): string | null {
  if (typeof window === "undefined") return null;
  const session = getSession();
  if (!session) return null;
  if (isExpired(session.token)) {
    clearSession();
    return null;
  }
  return session.token;
}

export function currentUser(): AuthUser | null {
  if (typeof window === "undefined") return null;
  const session = getSession();
  if (!session?.user) return null;
  return {
    email: session.user.email,
    fullName: session.user.name,
    avatarInitials: session.user.name
      ? session.user.name
          .split(" ")
          .map((n) => n[0])
          .join("")
          .toUpperCase()
      : session.user.email?.slice(0, 2).toUpperCase(),
  };
}

export function logout() {
  clearSession();
}
