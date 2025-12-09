"use client";

import axios from "axios";
import { clearSession, getToken, isExpired } from "./session";


// Mock API for local development
export const api = {
  post: async (path: string, payload: any) => {
    if (path === "/auth/signup") {
      // Simulate successful signup with token
      return { data: { token: "mock-jwt-token", message: "User registered successfully", user: { email: payload.username, fullName: "Test User" } } };
    }
    if (path === "/auth/login") {
      // Simulate successful login with mock token
      return { data: { token: "mock-jwt-token", user: { email: payload.username, fullName: "Test User" } } };
    }
    // Add more mock endpoints as needed
    return { data: {} };
  },
  get: async (path: string) => {
    // Simulate mock GET responses
    return { data: {} };
  },
};

export const withToken = () => "mock-jwt-token";
