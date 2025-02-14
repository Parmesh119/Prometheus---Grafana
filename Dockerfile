# Use Prometheus base image
FROM prom/prometheus as prometheus

# Copy Prometheus config
COPY prometheus.yml /etc/prometheus/prometheus.yml

# Use Grafana base image
FROM grafana/grafana as grafana

# Copy Grafana config
COPY grafana.ini /etc/grafana/grafana.ini

# Run both services in a single container
CMD ["sh", "-c", "prometheus --config.file=/etc/prometheus/prometheus.yml & grafana-server --config=/etc/grafana/grafana.ini"]
