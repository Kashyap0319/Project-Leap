import "./globals.css";
import { ReactNode } from "react";

export const metadata = {
  title: "API Monitoring Dashboard",
  description: "API Monitoring & Observability Platform",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="app-body">{children}</body>
    </html>
  );
}
