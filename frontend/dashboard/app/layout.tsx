import "./globals.css";
import { ReactNode } from "react";
import { ThemeProvider } from "../components/providers/theme-provider";
import { Toaster } from "sonner";

export const metadata = {
  title: "Leap API Center",
  description: "API Monitoring & Observability Platform",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="min-h-screen bg-background text-foreground">
        <ThemeProvider>
          <div className="flex min-h-screen flex-col">
            <main className="flex-1">{children}</main>
            <footer className="border-t border-border/60 bg-card/60 px-4 py-3 text-center text-xs text-muted-foreground">
              All rights reserved @shreyanshleap
            </footer>
          </div>
          <Toaster richColors position="top-right" closeButton />
        </ThemeProvider>
      </body>
    </html>
  );
}
