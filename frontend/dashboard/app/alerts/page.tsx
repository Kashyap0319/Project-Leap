"use client";
import { safeArray } from "../utils/safeArray";
import { useEffect, useRef, useState } from "react";
import { Bell, Loader2, Volume2 } from "lucide-react";
import { fetchAlerts } from "../../lib/data";
import { AutoRefreshControl } from "../../components/common/auto-refresh-control";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Skeleton } from "../../components/ui/skeleton";
import { useAutoRefresh } from "../../lib/hooks/useAutoRefresh";
import { toast } from "sonner";

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([] as any[]);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [interval, setIntervalMs] = useState(15000);
  const prevIds = useRef<Set<string>>(new Set());

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchAlerts();
      const incoming = new Set(safeArray(data).map((a) => a.id));
      const newOnes = [...incoming].filter((id) => !prevIds.current.has(id));
      if (prevIds.current.size > 0 && newOnes.length > 0) {
        toast.warning("⚠️ New alert detected", { description: `${newOnes.length} new alert(s)` });
        playChime();
      }
      prevIds.current = incoming;
      setAlerts(data as any[]);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to load alerts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  useAutoRefresh(autoRefresh, interval, load);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-foreground">Alerts</p>
          <h1 className="text-2xl font-bold">Current & historical alerts</h1>
        </div>
        <AutoRefreshControl enabled={autoRefresh} interval={interval} onToggle={setAutoRefresh} onIntervalChange={setIntervalMs} />
      </div>

      <Card>
        <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <CardTitle>Alerts</CardTitle>
            <CardDescription>Newest first. A bell badge shows the count.</CardDescription>
          </div>
          <Button variant="outline" size="sm" onClick={load} disabled={loading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Refresh now
          </Button>
        </CardHeader>
        <CardContent className="space-y-3">
          {loading ? (
            <Skeleton className="h-64 w-full" />
          ) : alerts.length === 0 ? (
            <p className="text-sm text-muted-foreground">No alerts right now. Enjoy the calm.</p>
          ) : (
            safeArray(alerts).map((alert) => (
              <div key={alert.id} className="flex items-start justify-between rounded-xl border border-border/60 bg-card/70 p-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <Badge variant={((alert.severity || "").toString().toLowerCase() === "critical") ? "destructive" : ((alert.severity || "").toString().toLowerCase() === "high") ? "warning" : "outline"}>
                      {(alert.severity || "").toString().toUpperCase()}
                    </Badge>
                    <span className="text-sm font-semibold">{alert.service} · {alert.endpoint}</span>
                  </div>
                  <p className="text-sm text-muted-foreground">{alert.message}</p>
                  <p className="text-xs text-muted-foreground">{new Date(alert.detectedAt || alert.triggeredAt).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-2">
                  {!alert.resolved && <Badge variant="destructive">Open</Badge>}
                  <Bell className="h-4 w-4 text-muted-foreground" />
                </div>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function playChime() {
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.frequency.value = 940;
  gain.gain.value = 0.12;
  osc.connect(gain).connect(ctx.destination);
  osc.start();
  osc.stop(ctx.currentTime + 0.2);
}
