**Prometheus**

- Prometheus is an open-source time-series database (TSDB) designed for monitoring and alerting in cloud-native environments. It collects metrics from various endpoints via its powerful query language, PromQL, and stores them in a time-series format.

- Prometheus use PromQL language which is flexible query language

- Prometheus is pull based monitoring system.

- Prometheus can also send alert message using its Alertmanager.

**What is metrics?**

- Metrics are numerical measurements in layperson terms.

- The term time series refers to the recording of changes over time.

- Let's assume you are running a web application and discover that it is slow. To learn what is happening with your application, you will need some information. For example, when the number of requests is high, the application may become slow. If you have the request count metric, you can determine the cause and increase the number of servers to handle the load.

**Thanos**

- Every tool has its limitations.

- Prometheus has alos.

- Thanos is an open-source project that extends Prometheus functionality to help to overcome its limitations particularly around long-term storage, scalability and high availability.

- The primary use of the Thanos tool is to enable scalable, long-term storage and querying of metrics collected by Prometheus instances across multiple clusters.

- Thanos is an open-source tool that extends Prometheus by adding features like long-term storage, high availability, and global querying.

- Thanos provides set of components to store objects.

- For better understanding: https://last9.io/blog/prometheus-vs-thanos/#:~:text=Thanos%20is%20an%20open%2Dsource%20tool%20that%20extends%20Prometheus%20by,better%20performance%20across%20large%20infrastructures.

**Grafana**

- Grafana is a open-source visualization and monitoring tool used to analyze and display time-series data. 

- Grafana open source software enables you to query, visualize, alert on, and explore your metrics, logs, and traces wherever they are stored.

- Grafana OSS provides you with tools to turn your time-series database (TSDB) data into insightful graphs and visualizations.

**This all tools are used in observability**

**What is Observability?**

- There are mainly four thing in Observability which comes after successfully deploying the application on cloud or kubernetes.

- That is Monitoring, Logging, Tracing, Alerting.

**Monitoring**

- Once we deployed our application we get to know that what is happening inside. The CPU use, network use, need to know metrics that is monitoring.

**Logging**

- Collect logs

**Tracing**

- If error comes need to understand how it comes, how to solve it that is tracing

**Alerting**

- Let's take that CPU use goes over 70% then need to send email that CPU usage is above thresold that is alerting.



- And If I put this all thing in visualization then it create dashboards.

- So for monitoring, metrics and tracing for that we are using Prometheus and for dashboards we are using Grafana.
