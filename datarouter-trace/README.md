# datarouter-trace

## About
datarouter-trace is a tool to collect and visualize distributed request traces.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-trace</artifactId>
	<version>0.0.24</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuidlder`.

```java
.addWebPlugin(new DatarouterTracePluginBuilder()
		...
		.build())
```

## Usage

The following are automatically traced:
* Node operations
* DatarouterHttpClientRequests
* Handlers

You can add additional tracing to the code with:

```java
try(var $ = TracerTool.startSpan("some trace name")){
	...
}
```

## Pipeline

Traces can be saved locally or sent to an external service over http. Saving traces can be taxing on the the local
 database or on the publishing client, since traces can be generated in high volume. Traces are first buffered in
 memory buffer, then in a queuing system (like SQS) before being sent to their respective locations.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
