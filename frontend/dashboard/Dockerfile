# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files
COPY frontend/dashboard/package*.json ./

# Install dependencies
RUN npm ci

# Copy source files
COPY frontend/dashboard/ ./

# Build the application
RUN npm run build

# Production stage
FROM node:20-alpine
WORKDIR /app

# Copy package files
COPY frontend/dashboard/package*.json ./

# Install production dependencies only
RUN npm ci --only=production && npm cache clean --force

# Copy built application from build stage
COPY --from=build /app/.next ./.next
COPY --from=build /app/next.config.js ./
COPY --from=build /app/tsconfig.json ./
COPY --from=build /app/tailwind.config.ts ./
COPY --from=build /app/postcss.config.js ./
# Copy public folder (created empty if needed)
COPY --from=build /app/public ./public

# Expose port
EXPOSE 3000

# Set environment to production
ENV NODE_ENV=production

# Start the application
CMD ["npm", "start"]

