# UI Service Dockerfile
# Simple Nginx-based static file server for the pricing management UI

FROM nginx:1.25-alpine

LABEL maintainer="DE-Store Development Team"
LABEL description="Management UI for DE-Store pricing service"
LABEL version="1.0.0"

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy UI files
COPY index.html /usr/share/nginx/html/
COPY login.html /usr/share/nginx/html/
COPY register.html /usr/share/nginx/html/
COPY loyalty.html /usr/share/nginx/html/
COPY app.js /usr/share/nginx/html/
COPY loyalty.js /usr/share/nginx/html/

# Copy custom Nginx configuration for SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 3000
EXPOSE 3000

# Install wget for healthcheck
RUN apk add --no-cache wget

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:3000 || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
