package com.Prometheus_Grafana.controller

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import kotlin.system.measureTimeMillis
import java.util.concurrent.TimeUnit

data class MetricsResponse(
    val data: String,
    val metrics: Map<String, Double>,
    val latency: Map<String, Double>
)

@RestController
@RequestMapping("/api")
class DemoController(private val meterRegistry: MeterRegistry) {

    private val endpoints = listOf("/data-demo", "/info", "/error", "/heavy-task")

    // Global metrics
    private val apiCounter = meterRegistry.counter("api_calls_total")
    private val apiSuccessCounter = meterRegistry.counter("api_success_total")
    private val apiErrorCounter = meterRegistry.counter("api_error_total")

    // Endpoint-specific counters and timers
    private val endpointCounters = endpoints.associateWith { endpoint ->
        meterRegistry.counter("endpoint_calls_total", "endpoint", endpoint)
    }

    private val endpointTimers = endpoints.associateWith { endpoint ->
        meterRegistry.timer("endpoint_latency", "endpoint", endpoint)
    }

    private fun recordApiMetrics(endpoint: String, status: String, timeTaken: Long) {
        apiCounter.increment()
        endpointCounters[endpoint]?.increment()
        endpointTimers[endpoint]?.record(timeTaken, TimeUnit.MILLISECONDS)

        if (status == "success") {
            apiSuccessCounter.increment()
        } else {
            apiErrorCounter.increment()
        }
    }

    private fun getEndpointMetrics(): Map<String, Double> {
        return endpoints.associateWith { endpoint ->
            endpointCounters[endpoint]?.count() ?: 0.0
        }
    }

    private fun getLatencyMetrics(): Map<String, Double> {
        return endpoints.associateWith { endpoint ->
            endpointTimers[endpoint]?.mean(TimeUnit.MILLISECONDS) ?: 0.0
        }
    }

    private fun createResponse(data: String, status: HttpStatus): ResponseEntity<MetricsResponse> {
        val metrics = getEndpointMetrics()
        val latency = getLatencyMetrics()
        val response = MetricsResponse(data, metrics, latency)
        return ResponseEntity.status(status).body(response)
    }



    var timeTaken = 0.toLong()
    @GetMapping("/data-demo")
    fun getData(): ResponseEntity<MetricsResponse> {
        var response: ResponseEntity<MetricsResponse>
        timeTaken = measureTimeMillis {
            try {
                Thread.sleep(100) // Simulating some work
                response = createResponse("Data Response", HttpStatus.OK)
                recordApiMetrics("/data-demo", "success", timeTaken)
            } catch (e: Exception) {
                response = createResponse("Error in Data API", HttpStatus.INTERNAL_SERVER_ERROR)
                recordApiMetrics("/data-demo", "error", timeTaken)
            }
        }
        return response
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<MetricsResponse> {
        var response: ResponseEntity<MetricsResponse>
        timeTaken = measureTimeMillis {
            try {
                Thread.sleep(150) // Simulating some work
                response = createResponse("Info Response", HttpStatus.OK)
                recordApiMetrics("/info", "success", timeTaken)
            } catch (e: Exception) {
                response = createResponse("Error in Info API", HttpStatus.INTERNAL_SERVER_ERROR)
                recordApiMetrics("/info", "error", timeTaken)
            }
        }
        return response
    }

    @GetMapping("/error")
    fun getError(): ResponseEntity<MetricsResponse> {
        var response: ResponseEntity<MetricsResponse>
        timeTaken = measureTimeMillis {
            Thread.sleep(50) // Simulating some work
            response = createResponse("Simulated Error", HttpStatus.INTERNAL_SERVER_ERROR)
            recordApiMetrics("/error", "error", timeTaken)
        }
        return response
    }

    @GetMapping("/heavy-task")
    fun performHeavyTask(): ResponseEntity<MetricsResponse> {
        var response: ResponseEntity<MetricsResponse>
        timeTaken = measureTimeMillis {
            try {
                Thread.sleep(2000) // Simulating a heavy operation
                response = createResponse("Heavy Task Completed", HttpStatus.OK)
                recordApiMetrics("/heavy-task", "success", timeTaken)
            } catch (e: Exception) {
                response = createResponse("Error in Heavy Task", HttpStatus.INTERNAL_SERVER_ERROR)
                recordApiMetrics("/heavy-task", "error", timeTaken)
            }
        }
        return response
    }

    @GetMapping("/metrics")
    fun getMetrics(): ResponseEntity<Map<String, Any>> {
        val metrics = mapOf(
            "calls" to getEndpointMetrics(),
            "latency" to getLatencyMetrics(),
            "total_calls" to apiCounter.count(),
            "success_calls" to apiSuccessCounter.count(),
            "error_calls" to apiErrorCounter.count()
        )
        return ResponseEntity.ok(metrics)
    }
}