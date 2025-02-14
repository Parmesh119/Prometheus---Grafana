package com.Prometheus_Grafana.model

data class MetricsResponse(
    val data: String,
    val metrics: Map<String, EndpointMetrics>,
    val latencyMs: Double
)