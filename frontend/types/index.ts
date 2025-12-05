export interface LogEntry {
  id?: string;
  timestamp: string;
  level: 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';
  message: string;
  source: string;
  metadata?: Record<string, any>;
  tags?: string[];
  userId?: string;
  version?: number;
}

export interface Alert {
  id?: string;
  type: 'ERROR_RATE' | 'RATE_LIMIT' | 'SYSTEM' | 'CUSTOM';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  title: string;
  description: string;
  source: string;
  threshold: number;
  currentValue: number;
  status: 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED' | 'DISMISSED';
  createdAt: string;
  resolvedAt?: string;
  resolvedBy?: string;
  resolution?: string;
  version?: number;
}

export interface User {
  username: string;
  email: string;
  token: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}
