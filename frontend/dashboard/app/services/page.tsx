"use client";

import { safeArray } from "../utils/safeArray";
import { useEffect, useMemo, useState } from "react";
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { fetchServices } from "../../lib/data";
import { AutoRefreshControl } from "../../components/common/auto-refresh-control";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Skeleton } from "../../components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { useAutoRefresh } from "../../lib/hooks/useAutoRefresh";
import { toast } from "sonner";

export default function ServicesPage() {
  const [services, setServices] = useState([] as any[]);
  const [selected, setSelected] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [interval, setIntervalMs] = useState(30000);

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchServices();
      setServices(data as any[]);
      if (!selected && data.length > 0) {
        setSelected(data[0].name);
      }
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to load services");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const stored = typeof window !== "undefined" ? localStorage.getItem("selectedService") : null;
    if (stored) setSelected(stored);
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useAutoRefresh(autoRefresh, interval, load);

  const service = useMemo(() => safeArray(services).find((s) => s.name === selected), [services, selected]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-foreground">Services</p>
          <h1 className="text-2xl font-bold">Service-centric analytics</h1>
        </div>
        <AutoRefreshControl enabled={autoRefresh} interval={interval} onToggle={setAutoRefresh} onIntervalChange={setIntervalMs} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Select a service</CardTitle>
          <CardDescription>View KPIs, latency trends, and endpoints.</CardDescription>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          {loading ? (
            <Skeleton className="col-span-2 h-10 w-full sm:col-span-4" />
          ) : (
            safeArray(services).map((svc) => (
              <Button
                key={svc.name}
                variant={svc.name === selected ? "default" : "outline"}
                onClick={() => {
                  setSelected(svc.name);
                  if (typeof window !== "undefined") localStorage.setItem("selectedService", svc.name);
                }}
                className="flex w-full items-center justify-center gap-2 rounded-full text-sm font-semibold"
              >
                <span
                  className={`h-2 w-2 rounded-full ${svc.errorRate > 0.05 ? "bg-destructive" : svc.errorRate > 0.01 ? "bg-amber-400" : "bg-emerald-400"}`}
                />
                {svc.name}
                <span className="hidden sm:flex">
                  <Sparkline points={svc.latencyTrend || []} />
                </span>
              </Button>
            ))
          )}
        </CardContent>
      </Card>

      {loading ? (
        <Skeleton className="h-96 w-full" />
      ) : service ? (
        <>
          <section className="grid gap-4 md:grid-cols-3">
            <Metric label="Requests" value={service.requests || 0} />
            <Metric label="Avg latency" value={`${(service.avgLatency || 0).toFixed(1)} ms`} />
            <Metric label="Error rate" value={`${((service.errorRate || 0) * 100).toFixed(2)}%`} badgeVariant={(service.errorRate || 0) > 0.05 ? "destructive" : "success"} />
          </section>

          <Card>
            <CardHeader>
              <CardTitle>Latency trend</CardTitle>
              <CardDescription>Recent performance for {service.name}</CardDescription>
            </CardHeader>
            <CardContent className="h-72">
              {service.latencyTrend && service.latencyTrend.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={service.latencyTrend}>
                    <XAxis 
                      dataKey="timestamp" 
                      tickFormatter={(v) => {
                        try {
                          return new Date(v as string).toLocaleTimeString();
                        } catch {
                          return v as string;
                        }
                      }}
                    />
                    <YAxis stroke="hsl(var(--muted-foreground))" />
                    <Tooltip 
                      labelFormatter={(v) => {
                        try {
                          return new Date(v as string).toLocaleString();
                        } catch {
                          return v as string;
                        }
                      }}
                      formatter={(value: number) => `${value} ms`}
                    />
                    <Line type="monotone" dataKey="latencyMs" stroke="#38bdf8" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-muted-foreground">
                  No latency trend data available
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Endpoints</CardTitle>
              <CardDescription>Performance per endpoint</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead scope="col">Endpoint</TableHead>
                    <TableHead scope="col">Method</TableHead>
                    <TableHead scope="col">Avg</TableHead>
                    <TableHead scope="col">P95</TableHead>
                    <TableHead scope="col">Error rate</TableHead>
                    <TableHead scope="col">Requests</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {safeArray(service.endpoints).length > 0 ? (
                    safeArray(service.endpoints).map((ep: any) => (
                      <TableRow key={`${ep.method}-${ep.path}`}>
                        <TableCell className="font-medium">{ep.path || "-"}</TableCell>
                        <TableCell>{ep.method || "GET"}</TableCell>
                        <TableCell>{(ep.avgLatency || 0).toFixed(1)} ms</TableCell>
                        <TableCell>{ep.p95Latency ? `${ep.p95Latency.toFixed(1)} ms` : "-"}</TableCell>
                        <TableCell>
                          <Badge variant={(ep.errorRate || 0) > 0.05 ? "destructive" : "success"}>{((ep.errorRate || 0) * 100).toFixed(2)}%</Badge>
                        </TableCell>
                        <TableCell>{ep.requestCount || 0}</TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-muted-foreground">
                        No endpoint data available for this service
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      ) : (
        <p className="text-sm text-muted-foreground">Select a service to view analytics.</p>
      )}
    </div>
  );
}

function Sparkline({ points }: { points: Array<{ latencyMs: number }> }) {
  if (!points?.length) return <span className="w-12" />;
  const slice = safeArray(points).slice(-10);
  const max = Math.max(...safeArray(slice).map((p) => p.latencyMs), 1);
  const path = safeArray(slice).map((p, i) => {
    const x = (i / Math.max(slice.length - 1, 1)) * 40;
    const y = 16 - (p.latencyMs / max) * 16;
    return `${x},${y}`;
  }).join(" ");
  return (
    <svg width="42" height="18" viewBox="0 0 42 18" className="text-primary">
      <polyline
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
        strokeLinecap="round"
        points={path}
      />
    </svg>
  );
}

function Metric({ label, value, badgeVariant }: { label: string; value: string | number; badgeVariant?: "success" | "destructive" | "outline" }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardDescription>{label}</CardDescription>
        {badgeVariant && <Badge variant={badgeVariant}>{value}</Badge>}
      </CardHeader>
      <CardContent>
        <p className="text-3xl font-semibold">{value}</p>
      </CardContent>
    </Card>
  );
}
