import type { NextConfig } from "next";
import path from 'path';

const nextConfig: NextConfig = {
  turbopack: {
    root: path.resolve(__dirname),
  },
  experimental: {
    turbo: {
      resolveAlias: {},
    },
  },
};

export default nextConfig;
