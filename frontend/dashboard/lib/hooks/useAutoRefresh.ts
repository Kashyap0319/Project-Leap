import { useEffect, useRef } from "react";

export function useAutoRefresh(enabled: boolean, intervalMs: number, callback: () => void) {
  const cbRef = useRef(callback);
  useEffect(() => {
    cbRef.current = callback;
  }, [callback]);

  useEffect(() => {
    if (!enabled) return;
    const id = window.setInterval(() => cbRef.current(), intervalMs);
    return () => window.clearInterval(id);
  }, [enabled, intervalMs]);
}
