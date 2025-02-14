#!/bin/bash

# Start Prometheus in background
/bin/prometheus --config.file=./prometheus.yml &

# Start Grafana in background
/usr/share/grafana/bin/grafana-server \
    --config=/etc/grafana/grafana.ini \
    --homepath=/usr/share/grafana &

# Keep container running
tail -f /dev/null
