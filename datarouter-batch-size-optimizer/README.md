# datarouter-batch-size-optimizer

## About
datarouter-batch-size-optimizer optimizes the batching operation by trying to find an optimal batch size to get the
 lowest per item latency. It uses a simple Quadratic regression with data collected during the previous call to
 that specific batching operation.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-batch-size-optimizer</artifactId>
	<version>0.0.70</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterBatchSizePluginBuilder(...)
		.build())
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
