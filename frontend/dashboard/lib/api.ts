"use client";

import axios from "axios";
import { getToken } from "./session";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080",
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token && token !== "null" && token !== "undefined") {
      config.headers = config.headers || {};
      config.headers["Authorization"] = `Bearer ${token}`;
    } else {
      if (config.headers && config.headers["Authorization"]) {
        delete config.headers["Authorization"];
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const api = axiosInstance;
