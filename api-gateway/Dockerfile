# API Gateway Dockerfile
# Nginx-based API gateway for DE-Store microservices

FROM nginx:1.25-alpine

LABEL maintainer="DE-Store Development Team"
LABEL description="API Gateway for DE-Store distributed retail system"
LABEL version="1.0.0"

# Remove default Nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/api-gateway.conf

# Create directory for logs
RUN mkdir -p /var/log/nginx

# Expose port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
