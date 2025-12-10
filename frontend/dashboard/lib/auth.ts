"use client";

import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { setSession, getToken, upsertUser, getSession, clearSession } from "./session";
import { api } from "./api";

// Re-export session utilities for backward compatibility
export { getSession, clearSession, isExpired } from "./session";


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
  let payload: Record<string, string>;
  if (path === "/auth/signup") {
    // Signup expects username, email, password
    if (!clean.username || !clean.email || !clean.password) {
      throw new Error("Username, email, and password are required");
    }
    payload = { username: clean.username, email: clean.email, password: clean.password };
  } else if (path === "/auth/login") {
    // Login expects username, password
    if (!clean.username || !clean.password) {
      throw new Error("Username and password are required");
    }
    payload = { username: clean.username, password: clean.password };
  } else {
    payload = clean;
  }
  try {
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

const AUTH_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL
  ? `${process.env.NEXT_PUBLIC_API_BASE_URL.replace(/\/$/, "")}/auth`
  : "http://localhost:8080/auth";

export const signup = async (username: string, email: string, password: string) => {
  const response = await axios.post(
    `${AUTH_BASE_URL}/signup`,
    { username, email, password },
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  if (response.data.token) {
    setSession(response.data.token, { email });
  }
  return response.data;
};

export const login = async (username: string, password: string) => {
  if (!username || !password) {
    throw new Error("Username and password are required");
  }
  try {
    const response = await axios.post(
      `${AUTH_BASE_URL}/login`,
      { username: username.trim(), password: password.trim() },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );
    if (response.data.token) {
      setSession(response.data.token, { email: username });
    }
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.errors) {
      const errors = error.response.data.errors as Array<{ field: string; message: string }>;
      const errorMessage = errors.map((e) => e.message).join(", ");
      throw new Error(errorMessage);
    }
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw error;
  }
};

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
  return getToken();
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
