package com.Prometheus_Grafana.controller
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Gauge
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class EndpointMetrics(
    val totalCalls: Double,
    val successCalls: Double,
    val failedCalls: Double,
    val activeRequests: Double // New field to track active requests
)

data class MetricsResponse(
    val data: String,
    val metrics: Map<String, EndpointMetrics>,
    val latencyMs: Double
)

@RestController
@RequestMapping("/api")
class DemoController(private val meterRegistry: MeterRegistry) {

    private val endpoints = listOf("/data-demo", "/info", "/error", "/heavy-task")

    // Track active requests per endpoint
    private val activeRequests = ConcurrentHashMap<String, Double>().apply {
        endpoints.forEach { this[it] = 0.0 }
    }

    // Register Gauges for active requests
    private val activeRequestGauges = endpoints.associateWith { endpoint ->
        Gauge.builder("active_requests_per_endpoint") { activeRequests[endpoint] ?: 0.0 }
            .tag("endpoint", endpoint)
            .register(meterRegistry)
    }

    // Global metrics
    private val apiCounter = meterRegistry.counter("api_calls_total")
    private val apiSuccessCounter = meterRegistry.counter("api_success_total")
    private val apiErrorCounter = meterRegistry.counter("api_error_total")
    private val apiLatency = meterRegistry.timer("api_latency_milliseconds")

    // Endpoint-specific counters
    private val endpointCounters = endpoints.associateWith { endpoint ->
        meterRegistry.counter("endpoint_calls_total", "endpoint", endpoint)
    }
    private val endpointSuccessCounters = endpoints.associateWith { endpoint ->
        meterRegistry.counter("endpoint_success_total", "endpoint", endpoint)
    }
    private val endpointErrorCounters = endpoints.associateWith { endpoint ->
        meterRegistry.counter("endpoint_error_total", "endpoint", endpoint)
    }

    private fun recordApiMetrics(endpoint: String, status: String, timeTaken: Long) {
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
    }

    private fun getEndpointMetrics(): Map<String, EndpointMetrics> {
        return endpoints.associateWith { endpoint ->
            EndpointMetrics(
                totalCalls = endpointCounters[endpoint]?.count() ?: 0.0,
                successCalls = endpointSuccessCounters[endpoint]?.count() ?: 0.0,
                failedCalls = endpointErrorCounters[endpoint]?.count() ?: 0.0,
                activeRequests = activeRequests[endpoint] ?: 0.0 // Include active request count
            )
        }
    }

    private fun createResponse(data: String, status: HttpStatus, latencyMs: Double): ResponseEntity<MetricsResponse> {
        val metrics = getEndpointMetrics()
        val response = MetricsResponse(data, metrics, latencyMs)
        return ResponseEntity.status(status).body(response)
    }

    private fun simulateWork(complexity: Int): String {
        val dataSize = complexity * 1000
        val data = ByteArray(dataSize) { Random.nextInt().toByte() }
        val hash = data.contentHashCode()
        return "Processed data of size $dataSize and hash $hash"
    }

    private fun trackActiveRequests(endpoint: String, isStart: Boolean) {
        synchronized(activeRequests) {
            activeRequests[endpoint] = (activeRequests[endpoint] ?: 0.0) + if (isStart) 1 else -1
        }
    }

    @GetMapping("/data-demo")
    fun getData(): ResponseEntity<MetricsResponse> {
        trackActiveRequests("/data-demo", true) // Increment active requests
        val startTime = System.nanoTime()
        val response: ResponseEntity<MetricsResponse> = try {
            val dataResult = simulateWork(1)
            createResponse("Data Response: $dataResult", HttpStatus.OK, 0.0)
        } catch (e: Exception) {
            createResponse("Error in Data API: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        }
        val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
        recordApiMetrics("/data-demo", status, timeTaken)
        trackActiveRequests("/data-demo", false) // Decrement active requests
        return response
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<MetricsResponse> {
        trackActiveRequests("/info", true)
        val startTime = System.nanoTime()
        val response: ResponseEntity<MetricsResponse> = try {
            val dataResult = simulateWork(2)
            createResponse("Info Response: $dataResult", HttpStatus.OK, 0.0)
        } catch (e: Exception) {
            createResponse("Error in Info API: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        }
        val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
        recordApiMetrics("/info", status, timeTaken)
        trackActiveRequests("/info", false)
        return response
    }

    @GetMapping("/error")
    fun getError(): ResponseEntity<MetricsResponse> {
        trackActiveRequests("/error", true)
        val startTime = System.nanoTime()
        val response: ResponseEntity<MetricsResponse>
        try {
            throw RuntimeException("Simulated Error in /error endpoint")
        } catch (e: Exception) {
            response = createResponse("Simulated Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            recordApiMetrics("/error", "error", timeTaken)
            trackActiveRequests("/error", false)
        }
        return response
    }

    @GetMapping("/heavy-task")
    fun performHeavyTask(): ResponseEntity<MetricsResponse> {
        trackActiveRequests("/heavy-task", true)
        val startTime = System.nanoTime()
        val response: ResponseEntity<MetricsResponse> = try {
            Thread.sleep(10000)
            val dataResult = simulateWork(10)
            createResponse("Heavy Task Completed: $dataResult", HttpStatus.OK, 0.0)
        } catch (e: Exception) {
            createResponse("Error in Heavy Task: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        }
        val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
        recordApiMetrics("/heavy-task", status, timeTaken)
        trackActiveRequests("/heavy-task", false)
        return response
    }

    @GetMapping("/metrics")
    fun getMetrics(): ResponseEntity<Map<String, Any>> {
        val detailedMetrics = getEndpointMetrics()
        val allMetrics = mapOf(
            "endpoints" to detailedMetrics,
            "global" to mapOf(
                "total_calls" to apiCounter.count(),
                "success_calls" to apiSuccessCounter.count(),
                "error_calls" to apiErrorCounter.count(),
                "mean_latency_ms" to apiLatency.mean(TimeUnit.MILLISECONDS)
            )
        )
        return ResponseEntity.ok(allMetrics)
    }
}