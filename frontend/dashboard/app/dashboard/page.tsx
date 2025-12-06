"use client";

import { useEffect, useMemo, useState } from "react";
import { fetchLogs, fetchIncidents, resolveIncident, logout, currentUser, requireAuth } from "../../lib/auth";

type Log = {
  service: string;
  endpoint: string;
  status: number;
  latencyMs: number;
  rateLimited: boolean;
  timestamp: number;
};

type Incident = {
  id: string;
  service: string;
  endpoint: string;
  type: string;
  status: string;
  firstSeen: number;
  lastSeen: number;
  occurrences: number;
  severity: string;
  version: number;
};

const statuses = [200, 400, 401, 403, 404, 429, 500, 502, 503];

export default function Dashboard() {
  const [logs, setLogs] = useState<Log[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [filters, setFilters] = useState({
    service: "",
    endpoint: "",
    status: "",
    slow: false,
    broken: false,
    rateLimited: undefined as boolean | undefined,
  });

  const load = async () => {
    try {
      setLoadError(null);
      const data = await fetchLogs({
        service: filters.service || undefined,
        endpoint: filters.endpoint || undefined,
        status: filters.status ? Number(filters.status) : undefined,
        slow: filters.slow || undefined,
        broken: filters.broken || undefined,
        rateLimited: filters.rateLimited,
      });
      setLogs(data);
      const inc = await fetchIncidents();
      setIncidents(inc);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load data";
      setLoadError(message);
    }
  };

  useEffect(() => {
    requireAuth();
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const analytics = useMemo(() => {
    const slow = logs.filter((l) => l.latencyMs > 500).length;
    const broken = logs.filter((l) => l.status >= 500).length;
    const rate = logs.filter((l) => l.rateLimited).length;
    const byEndpoint = logs.reduce<Record<string, { sum: number; count: number }>>((acc, l) => {
      acc[l.endpoint] = acc[l.endpoint] || { sum: 0, count: 0 };
      acc[l.endpoint].sum += l.latencyMs;
      acc[l.endpoint].count += 1;
      return acc;
    }, {});
    const avgLatency = Object.entries(byEndpoint).map(([ep, v]) => ({ endpoint: ep, avg: v.sum / v.count }));
    const topSlow = [...avgLatency].sort((a, b) => b.avg - a.avg).slice(0, 5);
    const trend = logs.map((l) => ({ t: l.timestamp, err: l.status >= 500 ? 1 : 0 }));
    return { slow, broken, rate, avgLatency, topSlow, trend };
  }, [logs]);

  const applyFilters = async () => {
    await load();
  };

  const onResolve = async (inc: Incident) => {
    try {
      await resolveIncident(inc.id, inc.version);
      await load();
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to resolve incident";
      setLoadError(message);
    }
  };

  return (
    <div className="page-shell">
      <div className="glass-panel top-bar">
        <div className="brand">
          <div className="brand-mark" />
          <div>
            <div className="brand-title">Project Leap · API Pulse</div>
            <div className="muted" style={{ fontSize: 13 }}>Real-time reliability cockpit</div>
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          {currentUser() && <div className="pill">Hi, {currentUser()}</div>}
          <button className="btn secondary" onClick={() => { logout(); window.location.href = "/login"; }}>Logout</button>
          <div className="pill">Live · {new Date().toLocaleTimeString()}</div>
        </div>
      </div>

      {loadError && (
        <div className="glass-panel" style={{ marginTop: 16, border: "1px solid var(--danger)", color: "var(--danger)" }}>
          <strong>API error:</strong> {loadError}
        </div>
      )}

      <section className="hero">
        <h1>Service health at a glance</h1>
        <p>Track latency, failures, rate limits, and incidents in one sleek cockpit. Apply filters and resolve noise quickly.</p>
      </section>

      <section className="metrics-grid">
        <div className="glass-panel metric-card">
          <div className="metric-label">Slow API count</div>
          <div className="metric-value">{analytics.slow}</div>
          <div className="metric-trend">Threshold &gt; 500ms</div>
        </div>
        <div className="glass-panel metric-card">
          <div className="metric-label">Broken API count</div>
          <div className="metric-value" style={{ color: analytics.broken ? "var(--danger)" : undefined }}>{analytics.broken}</div>
          <div className="metric-trend">5xx responses</div>
        </div>
        <div className="glass-panel metric-card">
          <div className="metric-label">Rate-limit violations</div>
          <div className="metric-value">{analytics.rate}</div>
          <div className="metric-trend">HTTP 429 detections</div>
        </div>
      </section>

      <section className="card-grid">
        <div className="glass-panel card">
          <h3>Latency by endpoint</h3>
          <div className="trend-list">
            {analytics.avgLatency.map((x) => (
              <div key={x.endpoint} className="trend-item">
                <div style={{ color: "var(--text)", fontWeight: 600 }}>{x.endpoint}</div>
                <div>{x.avg.toFixed(1)} ms avg</div>
              </div>
            ))}
          </div>
          <h3 style={{ marginTop: 14 }}>Top 5 slowest</h3>
          <div className="trend-list">
            {analytics.topSlow.map((x) => (
              <div key={x.endpoint} className="trend-item">
                <div style={{ color: "var(--text)", fontWeight: 600 }}>{x.endpoint}</div>
                <div>{x.avg.toFixed(1)} ms</div>
              </div>
            ))}
          </div>
        </div>

        <div className="glass-panel card">
          <h3>Incidents</h3>
          <div className="incidents">
            {incidents.map((inc) => (
              <div key={inc.id} className="incident-row">
                <div className="incident-meta">
                  <strong>{inc.service}{inc.endpoint}</strong>
                  <span className="muted">{inc.type} · v{inc.version}</span>
                  <div className={`status-chip ${inc.status === "OPEN" ? "status-open" : "status-resolved"}`}>
                    <span>{inc.status === "OPEN" ? "Open" : "Resolved"}</span>
                    <span className="muted">{new Date(inc.lastSeen).toLocaleTimeString()}</span>
                  </div>
                </div>
                {inc.status === "OPEN" ? (
                  <button className="btn" style={{ padding: "8px 12px" }} onClick={() => onResolve(inc)}>Resolve</button>
                ) : (
                  <span className="badge">Closed</span>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="glass-panel card" style={{ marginTop: 14 }}>
        <div style={{ display: "flex", justifyContent: "space-between", gap: 12, alignItems: "center", marginBottom: 12 }}>
          <h3 style={{ margin: 0 }}>Live traffic</h3>
          <div style={{ display: "flex", gap: 8 }}>
            <button className="btn secondary" onClick={applyFilters}>Apply filters</button>
            <button className="btn" onClick={load}>Refresh</button>
          </div>
        </div>

        <div className="filters" style={{ marginBottom: 14 }}>
          <input className="input" placeholder="Service" value={filters.service} onChange={(e) => setFilters({ ...filters, service: e.target.value })} />
          <input className="input" placeholder="Endpoint" value={filters.endpoint} onChange={(e) => setFilters({ ...filters, endpoint: e.target.value })} />
          <select className="input" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">Status</option>
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={filters.slow}
              onChange={(e) => setFilters({ ...filters, slow: e.target.checked })}
            />
            <span>slow &gt; 500ms</span>
          </label>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={filters.broken}
              onChange={(e) => setFilters({ ...filters, broken: e.target.checked })}
            />
            <span>broken 5xx</span>
          </label>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={filters.rateLimited ?? false}
              onChange={(e) => setFilters({ ...filters, rateLimited: e.target.checked })}
            />
            <span>rate-limit hits</span>
          </label>
        </div>

        <div className="table-card" style={{ overflowX: "auto" }}>
          <table>
            <thead>
              <tr>
                <th>Service</th>
                <th>Endpoint</th>
                <th>Status</th>
                <th>Latency</th>
                <th>Rate-limit</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l, i) => (
                <tr key={i}>
                  <td>{l.service}</td>
                  <td>{l.endpoint}</td>
                  <td><span className="badge">{l.status}</span></td>
                  <td>{l.latencyMs} ms</td>
                  <td>{l.rateLimited ? "Yes" : "No"}</td>
                  <td>{new Date(l.timestamp).toLocaleTimeString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function Widget({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="glass-panel card">
      <div className="muted" style={{ fontSize: 13 }}>{title}</div>
      <div style={{ fontSize: 26, fontWeight: 700 }}>{children}</div>
    </div>
  );
}
