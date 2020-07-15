# datarouter-metric

## About
datarouter-metric is a tool that can be used to record counters or gauges on anything. Third party services or other
 modules that haven't been released yet can be used to ingest the data and create visuals/ graphs.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-metric</artifactId>
	<version>0.0.40</version>
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

* Count - Records an increment which is aggregated and flushed every 5 seconds
* Gauge - Records any specified value at a certain point in time

## Pipeline

Counters are held in memory for 5 second periods. Next they are asynchronously flushed to a queue which can be backed
by a service like SQS. Finally they are handled in batches by CountPublisher.

Gauges can be generated in high volume and overload the buffer, so they are first buffered in memory, which is backed
by an `ArrayBlockingQueue`. Next gauges are buffered by a queue which can be backed by services like SQS. Finally they
are handled by GaugePublisher.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
