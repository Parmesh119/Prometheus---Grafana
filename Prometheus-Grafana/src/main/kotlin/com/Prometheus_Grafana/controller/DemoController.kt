package com.Prometheus_Grafana.controller

import com.Prometheus_Grafana.model.EndpointMetrics
import com.Prometheus_Grafana.model.MetricsResponse
import com.Prometheus_Grafana.util.MetricsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.annotation.PostConstruct  // Import javax.annotation.PostConstruct if using Java
import java.util.concurrent.TimeUnit


@RestController
@RequestMapping("/api")
class DemoController {

    @Autowired
    private lateinit var metricsUtil: MetricsUtil

    private val endpoints = listOf("/api/data-demo", "/api/info", "/api/error", "/api/heavy-task") //Include the prefix


    @PostConstruct
    fun init() {
        endpoints.forEach { metricsUtil.registerEndpoint(it) }
    }

    private fun simulateWork(complexity: Int): String {
        val dataSize = complexity * 1000
        val data = ByteArray(dataSize) { kotlin.random.Random.nextInt().toByte() }
        val hash = data.contentHashCode()
        return "Processed data of size $dataSize and hash $hash"
    }

    @GetMapping("/data-demo")
    fun getData(): ResponseEntity<MetricsResponse> {
        val endpoint = "/api/data-demo"
        metricsUtil.trackActiveRequests(endpoint, true)
        val startTime = System.nanoTime()
        var status = "success"
        val response: ResponseEntity<MetricsResponse> = try {
            val dataResult = simulateWork(1)
            ResponseEntity.ok(MetricsResponse("Data Response: $dataResult", emptyMap(), 0.0))
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MetricsResponse("Error in Data API: ${e.message}", emptyMap(), 0.0))
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
        return response
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<MetricsResponse> {
        val endpoint = "/api/info"
        metricsUtil.trackActiveRequests(endpoint, true)
        val startTime = System.nanoTime()
        var status = "success"
        val response: ResponseEntity<MetricsResponse> = try {
            val dataResult = simulateWork(2)
            ResponseEntity.ok(MetricsResponse("Info Response: $dataResult", emptyMap(), 0.0))
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MetricsResponse("Error in Info API: ${e.message}", emptyMap(), 0.0))
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
        return response
    }

    @GetMapping("/error")
    fun getError(): ResponseEntity<MetricsResponse> {
        val endpoint = "/api/error"
        metricsUtil.trackActiveRequests(endpoint, true)
        val startTime = System.nanoTime()
        var status = "error"
        val response: ResponseEntity<MetricsResponse> = try {
            throw RuntimeException("Simulated Error in /error endpoint")
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MetricsResponse("Simulated Error: ${e.message}", emptyMap(), 0.0))
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
        return response
    }

    @GetMapping("/heavy-task")
    fun performHeavyTask(): ResponseEntity<MetricsResponse> {
        val endpoint = "/api/heavy-task"
        metricsUtil.trackActiveRequests(endpoint, true)
        val startTime = System.nanoTime()
        var status = "success"
        val response: ResponseEntity<MetricsResponse> = try {
            Thread.sleep(10000)
            val dataResult = simulateWork(10)
            ResponseEntity.ok(MetricsResponse("Heavy Task Completed: $dataResult", emptyMap(), 0.0))
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MetricsResponse("Error in Heavy Task: ${e.message}", emptyMap(), 0.0))
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
        return response
    }

    @GetMapping("/metrics")
    fun getMetrics(): ResponseEntity<Map<String, Any>> {
        val detailedMetrics = metricsUtil.getEndpointMetrics()
        val globalMetrics = metricsUtil.getGlobalMetrics()

        val allMetrics = mapOf(
            "endpoints" to detailedMetrics,
            "global" to globalMetrics
        )
        return ResponseEntity.ok(allMetrics)
    }
}