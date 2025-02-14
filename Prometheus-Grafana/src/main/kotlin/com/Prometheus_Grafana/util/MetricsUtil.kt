// src/main/kotlin/com/Prometheus_Grafana/util/MetricsUtil.kt
package com.Prometheus_Grafana.util

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import com.Prometheus_Grafana.model.EndpointMetrics
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class MetricsUtil(private val meterRegistry: MeterRegistry) {

    private val activeRequests = ConcurrentHashMap<String, Double>()

    // Global metrics
    private val apiCounter = meterRegistry.counter("api_calls_total")
    private val apiSuccessCounter = meterRegistry.counter("api_success_total")
    private val apiErrorCounter = meterRegistry.counter("api_error_total")
    private val apiLatency = meterRegistry.timer("api_latency_milliseconds")

    private val endpointCounters = ConcurrentHashMap<String, Counter>()
    private val endpointSuccessCounters = ConcurrentHashMap<String, Counter>()
    private val endpointErrorCounters = ConcurrentHashMap<String, Counter>()
    private val endpointLatencyTimers = ConcurrentHashMap<String, Timer>()

    init{

    }

    fun registerEndpoint(endpoint: String) {
        endpointCounters[endpoint] = meterRegistry.counter("endpoint_calls_total", "endpoint", endpoint)
        endpointSuccessCounters[endpoint] = meterRegistry.counter("endpoint_success_total", "endpoint", endpoint)
        endpointErrorCounters[endpoint] = meterRegistry.counter("endpoint_error_total", "endpoint", endpoint)
        endpointLatencyTimers[endpoint] = meterRegistry.timer("endpoint_latency_milliseconds", "endpoint", endpoint)

        activeRequests[endpoint] = 0.0
        Gauge.builder("active_requests_per_endpoint") { activeRequests[endpoint] ?: 0.0 }
            .tag("endpoint", endpoint)
            .register(meterRegistry)
    }

    fun trackActiveRequests(endpoint: String, isStart: Boolean) {
        synchronized(activeRequests) {
            activeRequests[endpoint] = (activeRequests[endpoint] ?: 0.0) + if (isStart) 1 else -1
        }
    }

    fun recordApiMetrics(endpoint: String, status: String, timeTaken: Long) {
        apiCounter.increment()
        apiLatency.record(timeTaken, TimeUnit.MILLISECONDS)
        endpointCounters[endpoint]?.increment()

        if (status == "success") {
            apiSuccessCounter.increment()
            endpointSuccessCounters[endpoint]?.increment()
        } else {
            apiErrorCounter.increment()
            endpointErrorCounters[endpoint]?.increment()
        }

        endpointLatencyTimers[endpoint]?.record(timeTaken, TimeUnit.MILLISECONDS)
    }

    fun getEndpointMetrics(): Map<String, EndpointMetrics> {
        return endpointCounters.keys.associateWith { endpoint ->
            EndpointMetrics(
                totalCalls = endpointCounters[endpoint]?.count() ?: 0.0,
                successCalls = endpointSuccessCounters[endpoint]?.count() ?: 0.0,
                failedCalls = endpointErrorCounters[endpoint]?.count() ?: 0.0,
                activeRequests = activeRequests[endpoint] ?: 0.0
            )
        }
    }

    fun getGlobalMetrics(): Map<String, Any> {
        return mapOf(
            "total_calls" to apiCounter.count(),
            "success_calls" to apiSuccessCounter.count(),
            "error_calls" to apiErrorCounter.count(),
            "mean_latency_ms" to apiLatency.mean(TimeUnit.MILLISECONDS)
        )
    }
}