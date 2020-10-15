# datarouter-instrumentation

datarouter-instrumentation is a set of interfaces and data transfer objects that the core datarouter modules use to emit monitoring data. These can include exceptions, counters, metrics, tracing data, and other things. An application can choose whether to add a listener to catch the emitted data, either with an in-house system, a 3rd party library like OpenTSDB or Zipkin, or other datarouter modules that haven't been released yet.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-instrumentation</artifactId>
	<version>0.0.53</version>
</dependency>
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
