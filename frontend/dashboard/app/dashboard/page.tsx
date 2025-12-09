"use client";

"use client";

import type React from "react";
"use client";
import { safeArray } from "../utils/safeArray";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Pie, PieChart, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { AlertCircle, ArrowRight, Clock4, Flame, Gauge, Loader2, MapPin, ShieldCheck, Volume2, VolumeX } from "lucide-react";
import { toast } from "sonner";
import { fetchAlerts, fetchIncidents, fetchLogs, fetchServices, resolveIncident } from "../../lib/data";
import { currentUser, fetchMe } from "../../lib/auth";
import { useAutoRefresh } from "../../lib/hooks/useAutoRefresh";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { AutoRefreshControl } from "../../components/common/auto-refresh-control";
import { Skeleton } from "../../components/ui/skeleton";

const REFRESH_DEFAULT = 15000;
const COLORS = ["#4ade80", "#f87171"];

export default function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [interval, setIntervalMs] = useState(REFRESH_DEFAULT);
  const [logs, setLogs] = useState<any[] | null>(null);
  const [alerts, setAlerts] = useState<any[] | null>(null);
  const [incidents, setIncidents] = useState<any[] | null>(null);
  const [services, setServices] = useState<any[] | null>(null);
    // Mock fallback for logs
    useEffect(() => {
      if (!logs) {
        setLogs([
          {
            id: "1",
            service: "test-service",
            endpoint: "/home",
            method: "GET",
            status: 200,
            latencyMs: 120,
            timestamp: new Date().toISOString()
          }
        ]);
      }
    }, [logs]);

    // Mock fallback for alerts
    useEffect(() => {
      if (!alerts) {
        setAlerts([
          {
            id: "1",
            service: "payment-service",
            message: "Slow response detected",
            severity: "HIGH",
            timestamp: new Date().toISOString()
          }
        ]);
      }
    }, [alerts]);

    // Mock fallback for services
    useEffect(() => {
      if (!services) {
        setServices([
          {
            name: "test-service",
            totalRequests: 12,
            errors: 3,
            avgLatency: 221
          }
        ]);
      }
    }, [services]);
  const [user, setUser] = useState(null as any);
  const [muteAlerts, setMuteAlerts] = useState(false);
  const [liveAsc, setLiveAsc] = useState(false);
  const [serviceFilter, setServiceFilter] = useState("all");
  const [methodFilter, setMethodFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [mounted, setMounted] = useState(false);
  const lastAlertIds = useRef<Set<string>>(new Set());
  const lastChimeAt = useRef<number>(0);

  // Keep live traffic stable and time-ordered (newest first) for the table.
  const liveLogs = useMemo(() => {
    const logsArray: any[] = safeArray(logs);
    const sorted = [...logsArray].sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
    return liveAsc ? [...sorted].reverse() : sorted;
  }, [logs, liveAsc]);

  const filteredLogs = useMemo(() => {
    return safeArray(liveLogs).filter((log: any) => {
      const serviceOk = serviceFilter === "all" || log.service === serviceFilter;
      const methodOk = methodFilter === "all" || (log.method || "").toUpperCase() === methodFilter;
      const statusOk =
        statusFilter === "all" ||
        (statusFilter === "2xx" && log.status >= 200 && log.status < 300) ||
        (statusFilter === "4xx" && log.status >= 400 && log.status < 500) ||
        (statusFilter === "5xx" && log.status >= 500);
      return serviceOk && methodOk && statusOk;
    });
  }, [liveLogs, methodFilter, serviceFilter, statusFilter]);

  const availableServices = useMemo(() => {
    const names = new Set<string>();
    safeArray(logs).forEach((l: any) => names.add(l.service));
    return Array.from(names).filter(Boolean).sort();
  }, [logs]);

  const availableMethods = useMemo(() => {
    const methods = new Set<string>();
    safeArray(logs).forEach((l: any) => methods.add((l.method || "").toUpperCase()));
    methods.delete("");
    return Array.from(methods).sort();
  }, [logs]);

  const copyCurl = useCallback(async (log: any) => {
    const target = log.url || log.endpoint || "/";
    const url = typeof target === "string" && target.startsWith("http") ? target : `https://api.example.com${target}`;
    const cmd = `curl -X ${log.method || "GET"} "${url}"`;
    try {
      await navigator.clipboard.writeText(cmd);
      toast.success("Copied cURL", { description: cmd });
    } catch (err) {
      toast.error("Copy failed");
    }
  }, []);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      const [logData, alertData, incidentData, serviceData, me] = await Promise.all([
        fetchLogs({ size: 200 }),
        fetchAlerts(),
        fetchIncidents(),
        fetchServices(),
        fetchMe().catch(() => null),
      ]);
      setLogs(logData as any);
      setAlerts(alertData as any);
      setIncidents(incidentData as any);
      setServices(serviceData as any);
      setUser(me || currentUser());

      const incomingIds = new Set((alertData as any[]).map((a) => a.id));
      const prev = lastAlertIds.current;
      const newOnes = [...incomingIds].filter((id) => !prev.has(id));
      if (newOnes.length > 0 && prev.size > 0) {
        toast.warning("⚠️ New alert detected", { description: `${newOnes.length} new alert(s) arrived.` });
        const now = Date.now();
        if (!muteAlerts && now - lastChimeAt.current > 5000) {
          playChime();
          lastChimeAt.current = now;
        }
      }
      lastAlertIds.current = incomingIds;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load dashboard data";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }, [muteAlerts]);

  useEffect(() => {
    const stored = typeof window !== "undefined" ? localStorage.getItem("muteAlerts") : null;
    if (stored) setMuteAlerts(stored === "true");
    const storedSort = typeof window !== "undefined" ? localStorage.getItem("liveSortAsc") : null;
    if (storedSort) setLiveAsc(storedSort === "true");
    const onMuteChange = (event: CustomEvent<boolean>) => setMuteAlerts(event.detail);
    window.addEventListener("mute-alerts-changed", onMuteChange as EventListener);
    setMounted(true);
    load();
    return () => window.removeEventListener("mute-alerts-changed", onMuteChange as EventListener);
  }, [load]);

  useAutoRefresh(autoRefresh, interval, load);

  const summary = useMemo(() => {
    const logsArray: any[] = safeArray(logs);
    const slow = logsArray.filter((l) => l.latencyMs > 500).length;
    const broken = logsArray.filter((l) => l.status >= 500).length;
    const rate = logsArray.filter((l) => l.rateLimited || l.status === 429).length;
    const success = logsArray.filter((l) => l.status < 400).length;
    const total = logsArray.length || 1;
    const error = total - success;

    const byEndpoint: Record<string, { sum: number; count: number }> = {};
    logsArray.forEach((l) => {
      const key = `${l.method || ""} ${l.endpoint}`;
      byEndpoint[key] = byEndpoint[key] || { sum: 0, count: 0 };
      byEndpoint[key].sum += l.latencyMs || 0;
      byEndpoint[key].count += 1;
    });
    const topSlow = Object.entries(byEndpoint)
      .map(([endpoint, value]) => ({ endpoint, avg: value.sum / value.count }))
      .sort((a, b) => b.avg - a.avg)
      .slice(0, 5);

    return { slow, broken, rate, success, error, topSlow, total };
  }, [logs]);

  const errorRateSeries = useMemo(() => {
    const buckets: Record<string, { errors: number; total: number }> = {};
    const logsArray: any[] = safeArray(logs);
    logsArray.forEach((l: any) => {
      const bucket = new Date(l.timestamp).setMinutes(new Date(l.timestamp).getMinutes(), 0, 0).toString();
      buckets[bucket] = buckets[bucket] || { errors: 0, total: 0 };
      buckets[bucket].total += 1;
      if (l.status >= 500) buckets[bucket].errors += 1;
    });
    return Object.entries(buckets)
      .map(([k, v]) => ({ timestamp: Number(k), errorRate: v.total > 0 ? (v.errors / v.total) * 100 : 0 }))
      .sort((a, b) => a.timestamp - b.timestamp);
  }, [logs]);

  const avgLatencyPerEndpoint = useMemo(() => {
    const byEndpoint: Record<string, { sum: number; count: number; method: string }> = {};
    const logsArray: any[] = safeArray(logs);
    logsArray.forEach((l: any) => {
      const key = `${l.method || ""} ${l.endpoint}`;
      byEndpoint[key] = byEndpoint[key] || { sum: 0, count: 0, method: l.method };
      byEndpoint[key].sum += l.latencyMs || 0;
      byEndpoint[key].count += 1;
    });
    return Object.entries(byEndpoint)
      .map(([endpoint, value]) => ({ endpoint, avg: value.sum / value.count }))
      .sort((a, b) => b.avg - a.avg)
      .slice(0, 10);
  }, [logs]);

  const serviceKpis = useMemo(() => safeArray(services).slice(0, 4), [services]);

  const resolve = async (incident: any) => {
    try {
      await resolveIncident(incident.id, incident.version);
      toast.success("Incident resolved 👍");
      await load();
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to resolve incident";
      toast.error(message);
    }
  };

  return (
    <div className="space-y-4">
      <header className="flex flex-col gap-2 rounded-2xl border border-border/70 bg-gradient-to-br from-primary/10 via-secondary/10 to-background p-6 shadow-lg shadow-primary/10">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-sm text-muted-foreground">Welcome to Leap API Center 🚀</p>
            <h1 className="text-2xl font-bold">API Monitoring Made Smart</h1>
            {user && (
              <p className="text-sm text-muted-foreground">
                Logged in as {user.fullName || user.email} · {user.email} · Location: {user.city || "Unknown"}, {user.country || "Unknown"} 🌍
              </p>
            )}
          </div>
          <AutoRefreshControl
            enabled={autoRefresh}
            interval={interval}
            onToggle={setAutoRefresh}
            onIntervalChange={setIntervalMs}
          />
        </div>
        <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
          <Badge variant="outline" className="gap-2">
            <Clock4 className="h-3.5 w-3.5" /> Last login {user?.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : "-"}
          </Badge>
          <Badge variant="success" className="gap-2">
            <ShieldCheck className="h-3.5 w-3.5" /> Protected route
          </Badge>
          <Button
            size="sm"
            variant={muteAlerts ? "outline" : "secondary"}
            className="gap-2"
            onClick={() => setMuteAlerts((v) => !v)}
          >
            {muteAlerts ? <VolumeX className="h-3.5 w-3.5" /> : <Volume2 className="h-3.5 w-3.5" />}
            {muteAlerts ? "Sound off" : "Sound on"}
          </Button>
        </div>
      </header>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Slow APIs" value={summary.slow} icon={<Gauge className="h-5 w-5 text-amber-400" />} helper="> 500ms" loading={loading} />
        <MetricCard label="Broken APIs" value={summary.broken} icon={<Flame className="h-5 w-5 text-destructive" />} helper="5xx responses" loading={loading} />
        <MetricCard label="Rate limit hits" value={summary.rate} icon={<AlertCircle className="h-5 w-5 text-primary" />} helper="HTTP 429" loading={loading} />
        <MetricCard label="APIs monitored" value={summary.total} icon={<MapPin className="h-5 w-5 text-emerald-300" />} helper="Live" loading={loading} />
      </section>

      {serviceKpis.length > 0 && (
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {safeArray(serviceKpis).map((svc: any) => (
            <Card key={svc.name}>
              <CardHeader>
                <CardTitle className="text-base">{svc.name}</CardTitle>
                <CardDescription>{svc.requests} reqs · {(svc.errorRate * 100).toFixed(2)}% errors</CardDescription>
              </CardHeader>
              <CardContent className="space-y-1 text-sm text-muted-foreground">
                <div className="flex items-center justify-between"><span>Avg latency</span><span className="font-semibold text-foreground">{svc.avgLatency.toFixed(1)} ms</span></div>
                <div className="flex items-center justify-between"><span>Error rate</span><Badge variant={svc.errorRate > 0.05 ? "destructive" : "success"}>{(svc.errorRate * 100).toFixed(2)}%</Badge></div>
              </CardContent>
            </Card>
          ))}
        </section>
      )}

      <section className="grid gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <div>
              <CardTitle>Success vs Error</CardTitle>
              <CardDescription>Distribution of request outcomes.</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-2">
            <div className="h-64 w-full">
              {loading ? (
                <Skeleton className="h-full w-full" />
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={[
                        { name: "Success", value: summary.success },
                        { name: "Error", value: summary.error },
                      ]}
                      dataKey="value"
                      innerRadius={55}
                      outerRadius={90}
                    >
                      {[0, 1].map((i) => (
                        <Cell key={i} fill={COLORS[i]} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>
            <div className="space-y-3 rounded-xl border border-border/60 bg-card/60 p-4">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Success</span>
                <Badge variant="success">{summary.success}</Badge>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Errors</span>
                <Badge variant="destructive">{summary.error}</Badge>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Total</span>
                <Badge variant="outline">{summary.total}</Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top 5 slow endpoints</CardTitle>
            <CardDescription>Average latency per endpoint</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-60 w-full">
              {loading ? (
                <Skeleton className="h-full w-full" />
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={summary.topSlow} layout="vertical" margin={{ left: 0 }}>
                    <XAxis type="number" hide />
                    <YAxis dataKey="endpoint" type="category" width={120} tick={{ fontSize: 12 }} />
                    <Tooltip cursor={{ fill: "hsl(var(--border))" }} />
                    <Bar dataKey="avg" fill="#38bdf8" radius={[6, 6, 6, 6]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </CardContent>
        </Card>
      </section>

      {mounted && (
      <section className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Error rate trend</CardTitle>
            <CardDescription>Errors as a percentage of total requests.</CardDescription>
          </CardHeader>
          <CardContent className="h-72">
            {loading ? (
              <Skeleton className="h-full w-full" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={errorRateSeries}>
                  <XAxis dataKey="timestamp" tickFormatter={(v) => new Date(v).toLocaleTimeString()} />
                  <YAxis unit="%" allowDecimals={false} domain={[0, 100]} />
                  <Tooltip labelFormatter={(v) => new Date(v as number).toLocaleString()} formatter={(v: number) => `${v.toFixed(1)}%`} />
                  <Bar dataKey="errorRate" fill="#f97316" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Avg latency by endpoint</CardTitle>
            <CardDescription>Top 10 endpoints by average latency.</CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-64 w-full" />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead scope="col">Endpoint</TableHead>
                    <TableHead scope="col">Avg</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {avgLatencyPerEndpoint.map((row) => (
                    <TableRow key={row.endpoint}>
                      <TableCell>{row.endpoint}</TableCell>
                      <TableCell>{row.avg.toFixed(1)} ms</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </section>
      )}

      {mounted && (
      <section className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent alerts</CardTitle>
            <CardDescription>Sorted by newest first</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {loading ? (
              <Skeleton className="h-40 w-full" />
            ) : (
              safeArray(alerts).map((alert) => (
                <div key={alert.id} className="flex items-start justify-between rounded-xl border border-border/60 bg-card/70 p-3">
                  <div>
                    <div className="flex items-center gap-2 text-sm font-semibold">
                      <Badge variant={((alert.severity || "").toString().toLowerCase() === "critical") ? "destructive" : ((alert.severity || "").toString().toLowerCase() === "high") ? "warning" : "outline"}>
                        {(alert.severity || "").toString().toUpperCase()}
                      </Badge>
                      <span>{alert.service} · {alert.endpoint}</span>
                    </div>
                    <p className="text-sm text-muted-foreground">{alert.message}</p>
                    <p className="text-xs text-muted-foreground">{new Date(alert.detectedAt || alert.triggeredAt).toLocaleString()}</p>
                  </div>
                  {!alert.resolved && <Badge variant="destructive">Open</Badge>}
                </div>
              ))
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Incidents</CardTitle>
            <CardDescription>Resolve incidents optimistically</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {loading ? (
              <Skeleton className="h-40 w-full" />
            ) : (
              safeArray(incidents).map((incident) => (
                <div key={incident.id} className="flex items-center justify-between rounded-xl border border-border/60 bg-card/70 p-3">
                  <div className="space-y-1">
                    <p className="text-sm font-semibold">{incident.service} · {incident.endpoint}</p>
                    <p className="text-xs text-muted-foreground">{incident.type} · {new Date(incident.lastSeen).toLocaleString()}</p>
                    <Badge variant={incident.status === "OPEN" ? "destructive" : "success"}>
                      {incident.status}
                    </Badge>
                  </div>
                  {incident.status === "OPEN" && (
                    <Button size="sm" onClick={() => resolve(incident)} className="gap-2">
                      <ArrowRight className="h-4 w-4" /> Resolve
                    </Button>
                  )}
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </section>
      )}

      {mounted && (
      <Card>
        <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <CardTitle>Live traffic</CardTitle>
            <CardDescription>Latest requests with performance signals, sorted by most recent.</CardDescription>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" size="sm" onClick={() => {
              const next = !liveAsc;
              setLiveAsc(next);
              if (typeof window !== "undefined") localStorage.setItem("liveSortAsc", String(next));
            }}>
              {liveAsc ? "Oldest first" : "Newest first"}
            </Button>
            <Button variant="outline" size="sm" onClick={load} disabled={loading}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Refresh
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-3 grid gap-2 md:grid-cols-3">
            <label className="flex items-center gap-2 text-sm text-muted-foreground">
              Service
              <select
                className="w-full rounded-md border border-border bg-background px-2 py-1 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                value={serviceFilter}
                onChange={(e) => setServiceFilter(e.target.value)}
              >
                <option value="all">All</option>
                {safeArray(availableServices).map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </label>
            <label className="flex items-center gap-2 text-sm text-muted-foreground">
              Method
              <select
                className="w-full rounded-md border border-border bg-background px-2 py-1 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                value={methodFilter}
                onChange={(e) => setMethodFilter(e.target.value)}
              >
                <option value="all">All</option>
                {safeArray(availableMethods).map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </label>
            <label className="flex items-center gap-2 text-sm text-muted-foreground">
              Status
              <select
                className="w-full rounded-md border border-border bg-background px-2 py-1 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">All</option>
                <option value="2xx">2xx</option>
                <option value="4xx">4xx</option>
                <option value="5xx">5xx</option>
              </select>
            </label>
          </div>
          {loading ? (
            <Skeleton className="h-64 w-full" />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead scope="col">Time</TableHead>
                  <TableHead scope="col">Service</TableHead>
                  <TableHead scope="col">Endpoint</TableHead>
                  <TableHead scope="col">Method</TableHead>
                  <TableHead scope="col">Status</TableHead>
                  <TableHead scope="col">Latency</TableHead>
                  <TableHead scope="col">Request</TableHead>
                  <TableHead scope="col">Response</TableHead>
                  <TableHead scope="col">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {safeArray(filteredLogs).map((log) => (
                  <TableRow key={log.id}>
                    <TableCell className="text-xs text-muted-foreground">{new Date(log.timestamp).toLocaleTimeString()}</TableCell>
                    <TableCell className="font-medium">{log.service}</TableCell>
                    <TableCell>{log.endpoint}</TableCell>
                    <TableCell>{log.method}</TableCell>
                    <TableCell>
                      <Badge variant={log.status >= 500 ? "destructive" : log.status >= 400 ? "warning" : "success"}>{log.status}</Badge>
                    </TableCell>
                    <TableCell>{log.latencyMs} ms</TableCell>
                    <TableCell>{log.requestSizeBytes ? `${log.requestSizeBytes} B` : "-"}</TableCell>
                    <TableCell>{log.responseSizeBytes ? `${log.responseSizeBytes} B` : "-"}</TableCell>
                    <TableCell>
                      <Button variant="outline" size="sm" onClick={() => copyCurl(log)}>
                        Copy cURL
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      )}
    </div>
  );
}

function MetricCard({ label, value, helper, icon, loading }: { label: string; value: number; helper: string; icon: React.ReactNode; loading?: boolean }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between">
        <div>
          <CardDescription>{label}</CardDescription>
          <CardTitle className="text-3xl">
            {loading ? <Skeleton className="h-8 w-16" /> : value}
          </CardTitle>
        </div>
        <div className="rounded-full bg-border/60 p-2 text-primary">{icon}</div>
      </CardHeader>
      <CardContent>
        <p className="text-xs text-muted-foreground">{helper}</p>
      </CardContent>
    </Card>
  );
}

function playChime() {
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.frequency.value = 880;
  gain.gain.value = 0.1;
  osc.connect(gain).connect(ctx.destination);
  osc.start();
  osc.stop(ctx.currentTime + 0.25);
}
