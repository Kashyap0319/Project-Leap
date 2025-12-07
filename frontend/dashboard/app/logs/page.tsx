"use client";

import { useEffect, useMemo, useState } from "react";
import { Loader2, Search, XCircle } from "lucide-react";
import { fetchLogs } from "../../lib/data";
import { AutoRefreshControl } from "../../components/common/auto-refresh-control";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Input } from "../../components/ui/input";
import { Badge } from "../../components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Skeleton } from "../../components/ui/skeleton";
import { useAutoRefresh } from "../../lib/hooks/useAutoRefresh";
import { toast } from "sonner";

const statusOptions = ["2xx", "4xx", "5xx", "429"];
const DEFAULT_PAGE_SIZE = 20;

export default function LogsPage() {
  const [loading, setLoading] = useState(true);
  const [logs, setLogs] = useState([] as any[]);
  const [page, setPage] = useState(0);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [interval, setIntervalMs] = useState(15000);
  const [filters, setFilters] = useState({
    search: "",
    service: "",
    status: "",
    errorsOnly: false,
    slowOnly: false,
    range: "24h",
  });

  const load = async () => {
    setLoading(true);
    try {
      const params: Record<string, any> = {
        page,
        size: DEFAULT_PAGE_SIZE,
        q: filters.search || undefined,
        service: filters.service || undefined,
        status: filters.status || undefined,
        slow: filters.slowOnly || undefined,
        errorsOnly: filters.errorsOnly || undefined,
        window: filters.range,
      };
      const data = await fetchLogs(params);
      setLogs(data as any[]);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to fetch logs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  useAutoRefresh(autoRefresh, interval, load);

  const totals = useMemo(() => {
    const slow = logs.filter((l) => l.latencyMs > 500).length;
    const errors = logs.filter((l) => l.status >= 400).length;
    return { slow, errors };
  }, [logs]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-foreground">Logs</p>
          <h1 className="text-2xl font-bold">Live traffic stream</h1>
        </div>
        <AutoRefreshControl enabled={autoRefresh} interval={interval} onToggle={setAutoRefresh} onIntervalChange={setIntervalMs} />
      </div>

      <Card>
        <CardHeader className="space-y-2">
          <CardTitle>Filters</CardTitle>
          <CardDescription>Search, slice, and focus on the traffic that matters.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                className="pl-9"
                placeholder="Search endpoint or payload"
                value={filters.search}
                onChange={(e) => setFilters((f) => ({ ...f, search: e.target.value }))}
              />
            </div>
            <Input placeholder="Service name" value={filters.service} onChange={(e) => setFilters((f) => ({ ...f, service: e.target.value }))} />
            <select
              className="h-11 rounded-md border border-input bg-background px-3 text-sm"
              value={filters.status}
              onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
            >
              <option value="">Status</option>
              {statusOptions.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
            <select
              className="h-11 rounded-md border border-input bg-background px-3 text-sm"
              value={filters.range}
              onChange={(e) => setFilters((f) => ({ ...f, range: e.target.value }))}
            >
              <option value="1h">Last 1h</option>
              <option value="24h">Last 24h</option>
              <option value="7d">Last 7d</option>
            </select>
          </div>
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <FilterChip active={filters.errorsOnly} onClick={() => setFilters((f) => ({ ...f, errorsOnly: !f.errorsOnly }))}>
              Errors only
            </FilterChip>
            <FilterChip active={filters.slowOnly} onClick={() => setFilters((f) => ({ ...f, slowOnly: !f.slowOnly }))}>
              Slow APIs only
            </FilterChip>
            <Button variant="outline" size="sm" onClick={() => setFilters({ search: "", service: "", status: "", errorsOnly: false, slowOnly: false, range: "24h" })}>
              Clear
            </Button>
            <Button size="sm" onClick={load} disabled={loading}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Apply
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <CardTitle>Logs table</CardTitle>
            <CardDescription>Sticky header, pagination, animated rows.</CardDescription>
          </div>
          <div className="flex items-center gap-3 text-xs text-muted-foreground">
            <Badge variant="warning">Slow {totals.slow}</Badge>
            <Badge variant="destructive">Errors {totals.errors}</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-3">
          {loading ? (
            <Skeleton className="h-72 w-full" />
          ) : (
            <div className="overflow-hidden rounded-xl border border-border/60">
              <div className="max-h-[520px] overflow-auto">
                <table className="min-w-full text-sm">
                  <thead className="sticky top-0 bg-card/95 text-xs uppercase text-muted-foreground">
                    <tr>
                      <th className="px-3 py-3 text-left">Time</th>
                      <th className="px-3 py-3 text-left">Service</th>
                      <th className="px-3 py-3 text-left">Endpoint</th>
                      <th className="px-3 py-3 text-left">Method</th>
                      <th className="px-3 py-3 text-left">Status</th>
                      <th className="px-3 py-3 text-left">Latency</th>
                      <th className="px-3 py-3 text-left">Req Size</th>
                      <th className="px-3 py-3 text-left">Res Size</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs.map((log) => (
                      <tr key={log.id} className="animate-[fadeIn_200ms_ease] border-b border-border/50">
                        <td className="px-3 py-2 text-xs text-muted-foreground">{new Date(log.timestamp).toLocaleTimeString()}</td>
                        <td className="px-3 py-2 font-medium">{log.service}</td>
                        <td className="px-3 py-2">{log.endpoint}</td>
                        <td className="px-3 py-2">{log.method}</td>
                        <td className="px-3 py-2">
                          <Badge variant={log.status >= 500 ? "destructive" : log.status >= 400 ? "warning" : "success"}>{log.status}</Badge>
                        </td>
                        <td className="px-3 py-2">{log.latencyMs} ms</td>
                        <td className="px-3 py-2 text-xs">{log.requestSizeBytes ? `${log.requestSizeBytes} B` : "-"}</td>
                        <td className="px-3 py-2 text-xs">{log.responseSizeBytes ? `${log.responseSizeBytes} B` : "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="flex items-center justify-between border-t border-border/60 bg-card/70 px-4 py-2 text-sm text-muted-foreground">
                <span>Page {page + 1}</span>
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                    Prev
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function FilterChip({ active, children, onClick }: { active: boolean; children: React.ReactNode; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold transition-all ${
        active ? "border-primary bg-primary/10 text-primary" : "border-border hover:border-primary/60"
      }`}
    >
      {active ? <XCircle className="h-3.5 w-3.5" /> : null}
      {children}
    </button>
  );
}
