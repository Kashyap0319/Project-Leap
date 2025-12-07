import { api } from "./api";

export type LogEntry = {
  id: string;
  service: string;
  endpoint: string;
  method: string;
  status: number;
  latencyMs: number;
  requestSizeBytes?: number;
  responseSizeBytes?: number;
  rateLimited?: boolean;
  timestamp: string;
};

export type Alert = {
  id: string;
  service: string;
  endpoint: string;
  severity: "info" | "warning" | "critical" | "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  message: string;
  detectedAt: string;
  resolved?: boolean;
};

export type Incident = {
  id: string;
  service: string;
  endpoint: string;
  type: string;
  status: "OPEN" | "RESOLVED";
  firstSeen: string;
  lastSeen: string;
  occurrences: number;
  severity: string;
  version?: number;
};

export type ServiceSummary = {
  name: string;
  requests: number;
  avgLatency: number;
  errorRate: number;
  latencyTrend: Array<{ timestamp: string; latencyMs: number }>;
  endpoints: Array<{
    path: string;
    method: string;
    avgLatency: number;
    p95Latency?: number;
    errorRate: number;
    requestCount: number;
  }>;
};

export async function fetchLogs(params: Record<string, string | number | boolean | undefined>) {
  const { data } = await api.get<LogEntry[]>("/api/logs", { params });
  return data;
}

export async function fetchAlerts() {
  const { data } = await api.get<Alert[]>("/api/alerts");
  return data;
}

export async function fetchIncidents() {
  const { data } = await api.get<Incident[]>("/api/incidents");
  return data;
}

export async function resolveIncident(id: string, version?: number) {
  const { data } = await api.patch(`/api/incidents/${id}/resolve`, { version: version ?? 0 });
  return data;
}

export async function fetchServices() {
  const { data } = await api.get<ServiceSummary[]>("/api/services");
  return data;
}
