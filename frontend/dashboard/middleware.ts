import { NextRequest, NextResponse } from "next/server";

const protectedRoutes = ["/dashboard", "/logs", "/alerts", "/services"];

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;
  const isProtected = protectedRoutes.some((route) => pathname.startsWith(route));
  if (!isProtected) return NextResponse.next();

  const token = req.cookies.get("leap_token")?.value;
  if (!token) {
    const loginUrl = new URL("/login", req.url);
    loginUrl.searchParams.set("next", pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard/:path*", "/logs/:path*", "/alerts/:path*", "/services/:path*"],
};
