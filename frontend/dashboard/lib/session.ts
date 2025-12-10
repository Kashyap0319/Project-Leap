import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";

const STORAGE_KEY = "leap.session";
const COOKIE_KEY = "leap_token";

export type StoredSession = {
  token: string;
  user?: {
    email: string;
    name?: string;
  };
};

export type JwtPayload = {
  exp?: number;
  sub?: string;
  email?: string;
  name?: string;
};

function decode(token: string): JwtPayload | null {
  try {
    return jwtDecode<JwtPayload>(token);
  } catch {
    return null;
  }
}

export function setSession(token: string, user?: { email: string; name?: string }) {
  const payload: StoredSession = { token, user };
  if (typeof window !== "undefined") {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    // Set cookie using js-cookie for better compatibility
    Cookies.set(COOKIE_KEY, token, { 
      path: "/", 
      sameSite: "lax",
      expires: 7 // 7 days
    });
  }
}

export function clearSession() {
  if (typeof window !== "undefined") {
    localStorage.removeItem(STORAGE_KEY);
    Cookies.remove(COOKIE_KEY, { path: "/" });
  }
}

export function getSession(): StoredSession | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    const cookieToken = Cookies.get(COOKIE_KEY);
    if (!cookieToken) return null;
    return { token: cookieToken };
  }
  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    return null;
  }
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  const session = getSession();
  const cookieToken = Cookies.get(COOKIE_KEY);
  if (session?.token) return session.token;
  if (cookieToken) return cookieToken;
  return null;
}

export function isExpired(token: string | null): boolean {
  if (!token) return true;
  const payload = decode(token);
  if (!payload?.exp) return false;
  const now = Date.now() / 1000;
  return payload.exp < now;
}

export function upsertUser(user: { email: string; name?: string }) {
  if (typeof window === "undefined") return;
  const session = getSession();
  if (!session) return;
  const next: StoredSession = { ...session, user };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
}
