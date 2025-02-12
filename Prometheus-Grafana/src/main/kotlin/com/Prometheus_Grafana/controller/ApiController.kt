package com.Prometheus_Grafana.controller

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/api")
class ApiController(private val meterRegistry: MeterRegistry) {

    private val apiCounter = meterRegistry.counter("api_calls_total")
    private val apiSuccessCounter = meterRegistry.counter("api_success_total")
    private val apiErrorCounter = meterRegistry.counter("api_error_total")
    private val apiLatency = meterRegistry.timer("api_latency_seconds")

    @GetMapping("/data")
    fun getData(): ResponseEntity<String> {
        apiCounter.increment()

        val timeTaken: Long = measureTimeMillis {
            try {
                // Simulate API processing
                if ((1..10).random() > 2) {
                    apiSuccessCounter.increment()
                    return ResponseEntity.ok("Success Response")

                } else {
                    apiErrorCounter.increment()
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Response")
                }
            } catch (e: Exception) {
                apiErrorCounter.increment()
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception Occurred")
            }
        }

        // Convert Long to Double explicitly
        apiLatency.record(timeTaken.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
        return ResponseEntity.ok("Success Response")
    }
}
