import api from './api';
import { LogEntry, Alert } from '@/types';

export const logService = {
  getLogs: async (params?: {
    level?: string;
    source?: string;
    startTime?: string;
    endTime?: string;
  }): Promise<LogEntry[]> => {
    const response = await api.get('/api/logs', { params });
    return response.data;
  },

  getLogById: async (id: string): Promise<LogEntry> => {
    const response = await api.get(`/api/logs/${id}`);
    return response.data;
  },

  createLog: async (log: Omit<LogEntry, 'id' | 'timestamp' | 'userId' | 'version'>): Promise<LogEntry> => {
    const response = await api.post('/api/logs', log);
    return response.data;
  },

  deleteLog: async (id: string): Promise<void> => {
    await api.delete(`/api/logs/${id}`);
  },
};

export const alertService = {
  getAlerts: async (status?: string): Promise<Alert[]> => {
    const response = await api.get('/api/alerts', { params: { status } });
    return response.data;
  },

  getAlertById: async (id: string): Promise<Alert> => {
    const response = await api.get(`/api/alerts/${id}`);
    return response.data;
  },

  resolveAlert: async (id: string, resolution: string): Promise<Alert> => {
    const response = await api.post(`/api/alerts/${id}/resolve`, { resolution });
    return response.data;
  },

  dismissAlert: async (id: string): Promise<Alert> => {
    const response = await api.post(`/api/alerts/${id}/dismiss`);
    return response.data;
  },
};
