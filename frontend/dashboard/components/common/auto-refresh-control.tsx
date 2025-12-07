"use client";

import { Switch } from "../ui/switch";
import { cn } from "../../lib/utils";

const intervals = [5000, 15000, 30000, 60000];

export function AutoRefreshControl({
  enabled,
  interval,
  onToggle,
  onIntervalChange,
  className,
}: {
  enabled: boolean;
  interval: number;
  onToggle: (value: boolean) => void;
  onIntervalChange: (value: number) => void;
  className?: string;
}) {
  return (
    <div className={cn("flex flex-wrap items-center gap-3 rounded-lg border border-border/60 bg-card/70 px-3 py-2", className)}>
      <div className="flex items-center gap-2">
        <Switch checked={enabled} onCheckedChange={onToggle} aria-label="Toggle auto refresh" />
        <span className="text-sm text-muted-foreground">Auto refresh</span>
      </div>
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        {intervals.map((ms) => (
          <button
            key={ms}
            onClick={() => onIntervalChange(ms)}
            className={cn(
              "rounded-full border px-3 py-1 transition-all",
              interval === ms ? "border-primary/60 bg-primary/10 text-primary" : "hover:border-border"
            )}
          >
            {ms / 1000}s
          </button>
        ))}
      </div>
    </div>
  );
}
