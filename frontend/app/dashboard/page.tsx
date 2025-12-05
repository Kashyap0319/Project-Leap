'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/lib/auth';
import { logService, alertService } from '@/lib/services';
import { LogEntry, Alert } from '@/types';

export default function DashboardPage() {
  const router = useRouter();
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [filterLevel, setFilterLevel] = useState('');
  const [filterSource, setFilterSource] = useState('');
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      router.push('/login');
      return;
    }
    setUser(currentUser);
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [logsData, alertsData] = await Promise.all([
        logService.getLogs(),
        alertService.getAlerts('OPEN'),
      ]);
      setLogs(logsData);
      setAlerts(alertsData);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilter = async () => {
    setLoading(true);
    try {
      const params: any = {};
      if (filterLevel) params.level = filterLevel;
      if (filterSource) params.source = filterSource;
      const logsData = await logService.getLogs(params);
      setLogs(logsData);
    } catch (error) {
      console.error('Error filtering logs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveAlert = async (alertId: string, resolution: string) => {
    try {
      await alertService.resolveAlert(alertId, resolution);
      loadData();
    } catch (error) {
      console.error('Error resolving alert:', error);
    }
  };

  const handleDismissAlert = async (alertId: string) => {
    try {
      await alertService.dismissAlert(alertId);
      loadData();
    } catch (error) {
      console.error('Error dismissing alert:', error);
    }
  };

  const handleLogout = () => {
    authService.logout();
    router.push('/login');
  };

  const getLogLevelColor = (level: string) => {
    switch (level) {
      case 'ERROR':
      case 'FATAL':
        return 'text-red-600';
      case 'WARN':
        return 'text-yellow-600';
      case 'INFO':
        return 'text-blue-600';
      default:
        return 'text-gray-600';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-800';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  };

  if (loading && !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Project Leap Dashboard</h1>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600">Welcome, {user?.username}</span>
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
        {/* Widgets */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-sm font-medium text-gray-500">Total Logs</h3>
            <p className="text-3xl font-bold text-gray-900">{logs.length}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-sm font-medium text-gray-500">Active Alerts</h3>
            <p className="text-3xl font-bold text-red-600">{alerts.length}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-sm font-medium text-gray-500">Error Logs</h3>
            <p className="text-3xl font-bold text-orange-600">
              {logs.filter((l) => l.level === 'ERROR' || l.level === 'FATAL').length}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-sm font-medium text-gray-500">Sources</h3>
            <p className="text-3xl font-bold text-blue-600">
              {new Set(logs.map((l) => l.source)).size}
            </p>
          </div>
        </div>

        {/* Alerts Section */}
        {alerts.length > 0 && (
          <div className="bg-white rounded-lg shadow mb-6 p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Active Alerts</h2>
            <div className="space-y-3">
              {alerts.map((alert) => (
                <div
                  key={alert.id}
                  className="border border-gray-200 rounded-lg p-4 flex justify-between items-start"
                >
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <span className={`px-2 py-1 rounded text-xs font-medium ${getSeverityColor(alert.severity)}`}>
                        {alert.severity}
                      </span>
                      <h3 className="font-semibold text-gray-900">{alert.title}</h3>
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{alert.description}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      Source: {alert.source} | Created: {new Date(alert.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => {
                        const resolution = prompt('Enter resolution notes:');
                        if (resolution && alert.id) {
                          handleResolveAlert(alert.id, resolution);
                        }
                      }}
                      className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700"
                    >
                      Resolve
                    </button>
                    <button
                      onClick={() => alert.id && handleDismissAlert(alert.id)}
                      className="px-3 py-1 bg-gray-600 text-white text-sm rounded hover:bg-gray-700"
                    >
                      Dismiss
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Logs Section */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Logs</h2>

          {/* Filters */}
          <div className="mb-4 flex space-x-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Level</label>
              <select
                value={filterLevel}
                onChange={(e) => setFilterLevel(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded text-gray-900"
              >
                <option value="">All</option>
                <option value="TRACE">TRACE</option>
                <option value="DEBUG">DEBUG</option>
                <option value="INFO">INFO</option>
                <option value="WARN">WARN</option>
                <option value="ERROR">ERROR</option>
                <option value="FATAL">FATAL</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Source</label>
              <input
                type="text"
                value={filterSource}
                onChange={(e) => setFilterSource(e.target.value)}
                placeholder="Filter by source..."
                className="px-3 py-2 border border-gray-300 rounded text-gray-900"
              />
            </div>
            <div className="flex items-end">
              <button
                onClick={handleFilter}
                className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
              >
                Apply Filters
              </button>
            </div>
          </div>

          {/* Logs Table */}
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Timestamp
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Level
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Source
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Message
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {logs.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                      No logs found
                    </td>
                  </tr>
                ) : (
                  logs.map((log) => (
                    <tr key={log.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getLogLevelColor(log.level)}`}>
                        {log.level}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {log.source}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        {log.message}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
