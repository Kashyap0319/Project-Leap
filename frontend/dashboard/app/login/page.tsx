"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2, SunMoon } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { login } from "../../lib/auth";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { useTheme } from "next-themes";

const schema = z.object({
  email: z.string().email("Valid email required"),
  password: z.string().min(6, "Minimum 6 characters"),
});

type FormValues = z.infer<typeof schema>;

function LoginContent() {
  const router = useRouter();
  const params = useSearchParams();
  const next = params.get("next") || "/dashboard";
  const { resolvedTheme, setTheme } = useTheme();
  const [showPassword, setShowPassword] = useState(false);
  const {
    handleSubmit,
    register,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: FormValues) => {
    try {
      // Always send email as username
      const { token } = await login(values.email, values.password);
      toast.success("Welcome back", { description: "Redirecting to dashboard" });
      if (token) {
        router.push(next);
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Login failed";
      toast.error(message);
    }
  };

  return (
    <main className="grid min-h-screen grid-cols-1 bg-background text-foreground lg:grid-cols-2">
      <section className="relative hidden h-full items-center justify-center overflow-hidden lg:flex">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-secondary/10 to-background" />
        <div className="relative z-10 max-w-xl space-y-4 px-12">
          <p className="inline-flex items-center rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
            Leap API Center
          </p>
          <h1 className="text-4xl font-bold leading-tight lg:text-5xl">
            Welcome to <span className="text-gradient">Leap API Center</span>
          </h1>
          <p className="text-lg text-muted-foreground">
            Monitor latency, errors, and reliability in real time with a cockpit built for API-first teams.
          </p>
        </div>
      </section>

      <section className="relative flex items-center justify-center px-4 py-12 lg:px-12">
        <div className="absolute left-0 right-0 top-0 flex items-center justify-between px-4 py-4 lg:px-8">
          <p className="text-sm font-semibold text-muted-foreground">Leap API Center</p>
          <Button
            variant="ghost"
            size="icon"
            aria-label="Toggle theme"
            onClick={() => setTheme(resolvedTheme === "light" ? "dark" : "light")}
          >
            <SunMoon className="h-5 w-5" />
          </Button>
        </div>
        <Card className="w-full max-w-md border-border/70 bg-card/80 backdrop-blur">
          <CardHeader className="space-y-2">
            <CardTitle>Sign in</CardTitle>
            <CardDescription>Securely access your observability workspace.</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input placeholder="you@company.com" type="email" autoComplete="email" {...register("email")} />
                {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Password</label>
                <div className="relative">
                  <Input
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    autoComplete="current-password"
                    {...register("password")}
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-3 flex items-center text-muted-foreground"
                    onClick={() => setShowPassword((prev) => !prev)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
                {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
              </div>
              <Button className="w-full" type="submit" disabled={isSubmitting}>
                {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Sign in
              </Button>
            </form>
            <div className="mt-6 text-sm text-muted-foreground">
              Need an account? <Link className="text-primary underline" href="/signup">Create one</Link>
            </div>
          </CardContent>
        </Card>
      </section>
    </main>
  );
}

export default function Login() {
  return (
    <Suspense fallback={<div className="flex min-h-screen items-center justify-center">Loading...</div>}>
      <LoginContent />
    </Suspense>
  );
}
