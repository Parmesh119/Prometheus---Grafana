# Use multi-stage build for smaller final image
FROM prom/prometheus:latest as prometheus
FROM grafana/grafana:latest as grafana

# Final image
FROM ubuntu:latest

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Copy Prometheus from its image
COPY --from=prometheus /bin/prometheus /bin/
COPY --from=prometheus /etc/prometheus /etc/prometheus

# Copy Grafana from its image
COPY --from=grafana /usr/share/grafana /usr/share/grafana
COPY --from=grafana /etc/grafana /etc/grafana

# Copy configuration files
COPY prometheus.yml /etc/prometheus/prometheus.yml
COPY start.sh /start.sh

RUN chmod +x /start.sh

# Expose ports
EXPOSE 3000 9090

# Start both services
CMD ["/start.sh"]
