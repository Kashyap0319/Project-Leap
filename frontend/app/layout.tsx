import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Project Leap - Log Management",
  description: "Log Management System with Dual MongoDB and Rate Limiting",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
