"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Bell, BellOff, LayoutDashboard, List, LogOut, Radar, Settings, SunMoon, Table as TableIcon } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { currentUser, logout } from "../../lib/auth";
import { cn } from "../../lib/utils";
import { Button } from "../ui/button";
import { Switch } from "../ui/switch";
import { Badge } from "../ui/badge";
import { useTheme } from "next-themes";
import { toast } from "sonner";

const nav = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/logs", label: "Logs", icon: TableIcon },
  { href: "/alerts", label: "Alerts", icon: Bell },
  { href: "/services", label: "Services", icon: Radar },
];

export function ProtectedFrame({ children, alertCount = 0 }: { children: React.ReactNode; alertCount?: number }) {
  const pathname = usePathname();
  const router = useRouter();
  const { resolvedTheme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [user, setUser] = useState<{ email?: string; fullName?: string; avatarInitials?: string } | null>(null);
  const [showAccountMenu, setShowAccountMenu] = useState(false);
  const [muteAlerts, setMuteAlerts] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => setMounted(true), []);

  useEffect(() => {
    setUser(currentUser());
    const stored = typeof window !== "undefined" ? localStorage.getItem("muteAlerts") : null;
    if (stored) setMuteAlerts(stored === "true");
  }, []);

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (showAccountMenu && menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setShowAccountMenu(false);
      }
    };
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setShowAccountMenu(false);
    };
    document.addEventListener("mousedown", handleClick);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("mousedown", handleClick);
      document.removeEventListener("keydown", handleKey);
    };
  }, [showAccountMenu]);

  const onLogout = () => {
    logout();
    toast.success("Signed out");
    router.push("/login");
  };

  const onSwitchAccount = () => {
    logout();
    router.push("/login");
  };

  const toggleMute = () => {
    const next = !muteAlerts;
    setMuteAlerts(next);
    if (typeof window !== "undefined") {
      localStorage.setItem("muteAlerts", String(next));
      window.dispatchEvent(new CustomEvent("mute-alerts-changed", { detail: next }));
    }
  };

  const themeSwitch = (
    <div className="flex items-center gap-2 text-xs text-muted-foreground">
      <SunMoon className="h-4 w-4" />
      <Switch
        checked={resolvedTheme !== "light"}
        onCheckedChange={(v: boolean) => setTheme(v ? "dark" : "light")}
        aria-label="Toggle theme"
      />
    </div>
  );

  const navItems = useMemo(() => nav, []);

  return (
    <div className="flex min-h-screen bg-background/60">
      <aside className="hidden w-64 border-r border-border/60 bg-card/70 p-4 lg:flex lg:flex-col">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-secondary font-black text-background">
            {user?.avatarInitials || "L"}
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Leap API Center</p>
            <p className="text-lg font-semibold">Observability</p>
            {user?.email && (
              <p className="text-xs text-muted-foreground">Signed in: {user.fullName || user.email} ({user.email})</p>
            )}
          </div>
        </div>
        <nav className="space-y-2">
          {navItems.map((item) => {
            const active = pathname === item.href;
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-semibold transition-colors",
                  active ? "bg-primary/15 text-primary" : "text-muted-foreground hover:bg-border/40"
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
                {item.href === "/alerts" && alertCount > 0 && (
                  <Badge variant="destructive" className="ml-auto">
                    {alertCount}
                  </Badge>
                )}
              </Link>
            );
          })}
        </nav>
        <div className="mt-auto space-y-3">
          {mounted && themeSwitch}
          <Button variant="ghost" className="w-full justify-start gap-3 text-sm" onClick={onLogout}>
            <LogOut className="h-4 w-4" />
            Logout
          </Button>
        </div>
      </aside>
      <div className="flex-1">
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-border/60 bg-background/80 px-4 py-3 backdrop-blur">
          <div className="flex items-center gap-3">
            <div className="lg:hidden">
              <List className="h-5 w-5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs uppercase text-muted-foreground">Leap API Center</p>
              <p className="text-base font-semibold">API Monitoring Made Smart</p>
            </div>
          </div>
          <div className="relative flex items-center gap-3">
            {mounted && themeSwitch}
            <Button
              variant={muteAlerts ? "outline" : "ghost"}
              size="icon"
              aria-label={muteAlerts ? "Unmute alerts" : "Mute alerts"}
              onClick={toggleMute}
            >
              {muteAlerts ? <BellOff className="h-5 w-5" /> : <Bell className="h-5 w-5" />}
              {alertCount > 0 && !muteAlerts && <span className="absolute -right-1 -top-1 h-2.5 w-2.5 rounded-full bg-destructive" />}
            </Button>
            <Button
              variant="ghost"
              size="icon"
              aria-label="Account options"
              onClick={() => setShowAccountMenu((v) => !v)}
            >
              <Settings className="h-5 w-5" />
            </Button>
            {showAccountMenu && (
              <div
                ref={menuRef}
                className="absolute right-0 top-12 w-44 rounded-lg border border-border/60 bg-card/90 p-2 shadow-lg"
              >
                <p className="px-2 pb-2 text-xs text-muted-foreground">{user?.email || "Account"}</p>
                <Button variant="ghost" className="w-full justify-start" onClick={onSwitchAccount}>
                  Switch account
                </Button>
                <Button variant="destructive" className="w-full justify-start" onClick={onLogout}>
                  Logout
                </Button>
              </div>
            )}
          </div>
        </header>
        <main className="mx-auto max-w-screen-2xl px-4 py-6 lg:px-8">{children}</main>
      </div>
    </div>
  );
}
