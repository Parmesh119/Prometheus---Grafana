package com.Prometheus_Grafana.controller

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import kotlin.system.measureTimeMillis
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class EndpointMetrics(
    val totalCalls: Double,
    val successCalls: Double,
    val failedCalls: Double
)

data class MetricsResponse(
    val data: String,
    val metrics: Map<String, EndpointMetrics>,
    val latencyMs: Double  // Explicitly named as milliseconds
)

@RestController
@RequestMapping("/api")
class DemoController(private val meterRegistry: MeterRegistry) {

    private val endpoints = listOf("/data-demo", "/info", "/error", "/heavy-task")

    // Global metrics
    private val apiCounter = meterRegistry.counter("api_calls_total")
    private val apiSuccessCounter = meterRegistry.counter("api_success_total")
    private val apiErrorCounter = meterRegistry.counter("api_error_total")
    private val apiLatency = meterRegistry.timer("api_latency_milliseconds") // Changed name for clarity

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
                failedCalls = endpointErrorCounters[endpoint]?.count() ?: 0.0
            )
        }
    }

    private fun createResponse(data: String, status: HttpStatus, latencyMs: Double): ResponseEntity<MetricsResponse> {
        val metrics = getEndpointMetrics()
        val response = MetricsResponse(data, metrics, latencyMs)
        return ResponseEntity.status(status).body(response)
    }


    private fun simulateWork(complexity: Int): String {
        // Simulate different levels of work based on complexity.
        val dataSize = complexity * 1000 // Vary data size.
        val data = ByteArray(dataSize) { Random.nextInt().toByte() } // Fill with random data.

        // Simulate CPU-bound work (e.g., hashing). This is a placeholder.
        val hash = data.contentHashCode()

        return "Processed data of size ${dataSize} and hash ${hash}"
    }

    var response: ResponseEntity<MetricsResponse> = ResponseEntity.ok().build()
    @GetMapping("/data-demo")
    fun getData(): ResponseEntity<MetricsResponse> {

        var timeTaken = 0L
        val startTime = System.nanoTime()
        try {
            // Simulate work without Thread.sleep
            val complexity = 1
            val dataResult = simulateWork(complexity)
            response = createResponse("Data Response: $dataResult", HttpStatus.OK, 0.0) //Include result in response

        } catch (e: Exception) {
            response = createResponse("Error in Data API: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0) //Include error message
        } finally {
            timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
            recordApiMetrics("/data-demo", status, timeTaken)
        }
        return createResponse(response.body?.data ?: "", response.statusCode as HttpStatus, timeTaken.toDouble())
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<MetricsResponse> {
        var timeTaken = 0L
        val startTime = System.nanoTime()
        try {
            // Simulate work without Thread.sleep
            val complexity = 2
            val dataResult = simulateWork(complexity)
            response = createResponse("Info Response: $dataResult", HttpStatus.OK, 0.0)
        } catch (e: Exception) {
            response = createResponse("Error in Info API: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        } finally {
            timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
            recordApiMetrics("/info", status, timeTaken)
        }
        return createResponse(response.body?.data ?: "", response.statusCode as HttpStatus, timeTaken.toDouble())
    }

    @GetMapping("/error")
    fun getError(): ResponseEntity<MetricsResponse> {
        var response: ResponseEntity<MetricsResponse>
        var timeTaken = 0L
        val startTime = System.nanoTime()
        try {
            // Simulate work without Thread.sleep, but force an error
            val complexity = 0 // No work required

            throw RuntimeException("Simulated Error in /error endpoint") // Simulate an exception
        } catch (e: Exception) {
            response = createResponse("Simulated Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        } finally {
            timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            recordApiMetrics("/error", "error", timeTaken)
        }
        return createResponse(response.body?.data ?: "", response.statusCode as HttpStatus, timeTaken.toDouble())
    }

    @GetMapping("/heavy-task")
    fun performHeavyTask(): ResponseEntity<MetricsResponse> {
        var timeTaken = 0L
        val startTime = System.nanoTime()
        try {
            // Simulate a more demanding task
            Thread.sleep(2000)
            val complexity = 10
            val dataResult = simulateWork(complexity)
            response = createResponse("Heavy Task Completed: $dataResult", HttpStatus.OK, 0.0)
        } catch (e: Exception) {
            response = createResponse("Error in Heavy Task: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, 0.0)
        } finally {
            timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            val status = if (response.statusCode == HttpStatus.OK) "success" else "error"
            recordApiMetrics("/heavy-task", status, timeTaken)
        }
        return createResponse(response.body?.data ?: "", response.statusCode as HttpStatus, timeTaken.toDouble())
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