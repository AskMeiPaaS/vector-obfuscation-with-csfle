# Stage 1: Dependencies
FROM node:20-alpine AS deps
WORKDIR /app

# Copy specifically from the frontend directory into the current WORKDIR
COPY frontend/package.json ./
COPY frontend/package-lock.json* ./

# Verify the file is there before running install
RUN ls -la /app && npm install

# Stage 2: Build
FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY frontend .
RUN npm run build

# Stage 3: Runner
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV production
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json

EXPOSE 3000
CMD ["npm", "start"]