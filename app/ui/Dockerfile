# Multi-stage build for production
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

# Production image with nginx
FROM nginx:alpine

# Copy built files
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Install shadow for user management
RUN apk add --no-cache shadow

# Create non-root user (kept for file ownership), but run as root to bind :80
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

# Change ownership to non-root user for files
RUN chown -R appuser:appuser /usr/share/nginx/html /var/cache/nginx /var/log/nginx
RUN touch /var/run/nginx.pid && chown -R appuser:appuser /var/run/nginx.pid

EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]
