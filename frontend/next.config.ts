import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  // Moved from experimental to top-level in Next.js 15
  outputFileTracingRoot: path.join(__dirname),

  // Optional: If you still hit the Turbopack panic, 
  // you can explicitly disable it here:
  // turbo: { enabled: false }
};

export default nextConfig;