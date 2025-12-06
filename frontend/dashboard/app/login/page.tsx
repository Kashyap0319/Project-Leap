"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login } from "../../lib/auth";

export default function Login() {
  const [username, setUser] = useState("");
  const [password, setPass] = useState("");
  const [error, setError] = useState("");
  const router = useRouter();

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setError("");
      const token = await login(username, password);
      localStorage.setItem("token", token);
      router.push("/dashboard");
    } catch (err: any) {
      setError(err?.message || "Login failed");
    }
  };

  return (
    <main className="login-shell">
      <div className="auth-left">
        <div>
          <h1>Project Leap</h1>
          <p>Command your API health and monitor performance in real time.</p>
        </div>
      </div>

      <div className="auth-right">
        <div>
          <h2>Welcome</h2>
          <p className="auth-subtext">Sign in to continue</p>
        </div>
        <form onSubmit={onSubmit}>
          <input
            className="input-style"
            placeholder="Username"
            value={username}
            onChange={(e) => setUser(e.target.value)}
          />
          <input
            className="input-style"
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPass(e.target.value)}
          />
          {error && <p className="error-text">{error}</p>}
          <button className="btn-primary" type="submit">Sign In</button>
        </form>

        <div className="divider-row">
          <div className="line" />
          <span className="muted-small">OR</span>
          <div className="line" />
        </div>

        <p className="muted-small">
          Don’t have an account? {" "}
          <Link className="btn-secondary-link" href="/signup">Create one</Link>
        </p>
      </div>
    </main>
  );
}
