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
  const { data } = await api.get<{ content: any[]; totalElements: number }>("/api/logs", { params });
  // Map backend response to frontend format
  const logs = (data.content || data || []).map((log: any) => ({
    id: log.id,
    service: log.service,
    endpoint: log.endpoint,
    method: log.method,
    status: log.statusCode || log.status, // Backend uses statusCode
    latencyMs: log.latencyMs,
    requestSizeBytes: log.requestSize || log.requestSizeBytes,
    responseSizeBytes: log.responseSize || log.responseSizeBytes,
    rateLimited: log.rateLimited,
    timestamp: log.timestamp,
  }));
  return logs;
}

export async function fetchAlerts() {
  const { data } = await api.get<Alert[]>("/api/alerts");
  // Map backend response to frontend format
  return (data || []).map((alert: any) => ({
    id: alert.id,
    service: alert.service,
    endpoint: alert.endpoint,
    severity: alert.severity,
    message: alert.message,
    detectedAt: alert.detectedAt,
    resolved: alert.resolved,
  }));
}

export async function fetchIncidents() {
  const { data } = await api.get<Incident[]>("/api/incidents");
  // Map backend response to frontend format
  return (data || []).map((incident: any) => ({
    id: incident.id,
    service: incident.service,
    endpoint: incident.endpoint,
    type: incident.type,
    status: incident.resolved ? "RESOLVED" : "OPEN", // Backend uses resolved boolean
    firstSeen: incident.firstSeen,
    lastSeen: incident.lastSeen,
    occurrences: incident.occurrences,
    severity: incident.severity,
    version: incident.version,
  }));
}

export async function resolveIncident(id: string, version?: number) {
  const { data } = await api.post(`/api/incidents/${id}/resolve`, { version: version ?? 0 });
  return data;
}

export async function fetchServices() {
  const { data } = await api.get<ServiceSummary[]>("/api/services");
  // Map backend response - ensure errorRate is decimal (0-1) and add latencyTrend
  return (data || []).map((service: any) => ({
    name: service.name,
    requests: service.requests,
    avgLatency: service.avgLatency,
    errorRate: typeof service.errorRate === 'number' 
      ? (service.errorRate > 1 ? service.errorRate / 100 : service.errorRate) // Fix if backend returns percentage
      : 0,
    latencyTrend: service.latencyTrend || [], // Add empty array if missing
    endpoints: (service.endpoints || []).map((ep: any) => ({
      path: ep.path,
      method: ep.method,
      avgLatency: ep.avgLatency,
      p95Latency: ep.p95Latency,
      errorRate: typeof ep.errorRate === 'number'
        ? (ep.errorRate > 1 ? ep.errorRate / 100 : ep.errorRate) // Fix if backend returns percentage
        : 0,
      requestCount: ep.requestCount,
    })),
  }));
}
