package com.Prometheus_Grafana.controller

import com.Prometheus_Grafana.util.MetricsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.annotation.PostConstruct  // Import javax.annotation.PostConstruct if using Java
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/sample")
class Sample {

    @Autowired
    private lateinit var metricsUtil: MetricsUtil // Inject MetricsUtil

    private val endpointName = "/sample/data"

    @PostConstruct // Use PostConstruct to ensure metricsUtil is initialized
    fun init() {
        metricsUtil.registerEndpoint(endpointName) // Register the endpoint
    }

    @GetMapping("/data")
    fun data(): String {
        val startTime = System.nanoTime()
        var status = "success"
        try {
            metricsUtil.trackActiveRequests(endpointName, true) // Start tracking

            // Simulate some work
            Thread.sleep(100)

            return "Data"
        } catch (e: Exception) {
            status = "error"
            throw e // Re-throw the exception to propagate the error
        } finally {
            metricsUtil.trackActiveRequests(endpointName, false) // Stop tracking
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpointName, status, timeTaken)
        }
    }


    @GetMapping("/metrics")
    fun getMetrics(): Map<String, Any> {
        val endpointMetrics = metricsUtil.getEndpointMetrics()
        val globalMetrics = metricsUtil.getGlobalMetrics()

        // Combine the endpoint and global metrics into a single map
        val allMetrics = mutableMapOf<String, Any>()
        allMetrics["endpoints"] = endpointMetrics
        allMetrics["global"] = globalMetrics

        return allMetrics
    }
}