# datarouter-metric

## About
datarouter-metric is a tool that can be used to record counters or gauges on anything. Third party services or other
 modules that haven't been released yet can be used to ingest the data and create visuals/ graphs.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-metric</artifactId>
	<version>0.0.30</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWeblugin(new DatarouterMetricPluginBuilder(...)
		.build()
```

## Usage
There are two types of metrics: Counts and Gauges.

* Count - Records an increment which is then flushed and averaged based on of the MetricPeriods.
* Gauge - Records any specified value at a certain point in time

Metric Periods
* 5 seconds
* 20 seconds
* 1 minute
* 5 minutes
* 20 minutes
* 1 hour
* 4 hours
* 1 day

Counts are partitioned by the MetricPeriod before they are sent to an external service, Gauges are not.

## Pipeline

Counters are buffered in a queuing system that can be backed like external services like SQS. Counts are then sent over
 http to an external service. 

Gagues can generated in high volume and can overload the buffer, so they are first buffered in memory, which is backed
 by an `ArrayBlockingQueue`. Then the gauges are buffered in a third party queueing system like SQS, and then are
 posted to an external service.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
