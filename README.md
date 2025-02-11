**Prometheus**

- Prometheus is an open-source time-series database (TSDB) designed for monitoring and alerting in cloud-native environments. It collects metrics from various endpoints via its powerful query language, PromQL, and stores them in a time-series format.

- Prometheus use PromQL language which is flexible query language

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

**Comparison**

Prometheus vs Thanos: A Comparison Table
Feature	Prometheus	Thanos
Purpose	Monitoring and alerting system for time-series data	Extends Prometheus with long-term storage, HA, and global querying
Developed By	CNCF (Cloud Native Computing Foundation)	CNCF (Cloud Native Computing Foundation)
Storage	Local storage (TSDB)	Uses object storage (S3, GCS, Azure Blob, etc.)
Scalability	Limited to a single instance	Horizontally scalable with multiple Prometheus instances
High Availability (HA)	Not built-in (requires federation/sharding)	Provides HA by deduplicating data from multiple Prometheus instances
Long-term Storage	Limited to local retention settings	Supports long-term storage via object stores
Global Querying	Queries only its own local TSDB	Allows querying data across multiple Prometheus instances
Deduplication	Not available	Supports deduplication across multiple Prometheus replicas
Downsampling	Not available	Supports downsampling to optimize storage and querying
Deployment Complexity	Simple (single binary)	More complex (multiple components like Querier, Store, Compact, Sidecar)
Use Case	Small to medium-sized deployments	Large-scale, multi-cluster, and long-term monitoring
