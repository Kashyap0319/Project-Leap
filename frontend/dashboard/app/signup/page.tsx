"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { signup } from "../../lib/auth";

export default function SignupPage() {
  const [username, setUser] = useState("");
  const [password, setPass] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const router = useRouter();

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setError("");
      const token = await signup(username, password);
      localStorage.setItem("token", token);
      setSuccess("Account created! Redirecting...");
      setTimeout(() => router.push("/dashboard"), 400);
    } catch (err: any) {
      setError(err?.message || "Sign up failed");
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
          <p className="auth-subtext">Create an account to get started</p>
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
          {success && <p className="muted-small">{success}</p>}
          <button className="btn-primary" type="submit">Sign Up</button>
        </form>

        <div className="divider-row">
          <div className="line" />
          <span className="muted-small">OR</span>
          <div className="line" />
        </div>

        <p className="muted-small">
          Already have an account? {" "}
          <Link className="btn-secondary-link" href="/login">Sign In</Link>
        </p>
      </div>
    </main>
  );
}
