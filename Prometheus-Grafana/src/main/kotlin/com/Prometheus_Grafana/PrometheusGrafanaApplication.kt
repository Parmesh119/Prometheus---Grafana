package com.Prometheus_Grafana

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrometheusGrafanaApplication

fun main(args: Array<String>) {
	runApplication<PrometheusGrafanaApplication>(*args)
}
