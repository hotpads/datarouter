# datarouter-load-test

Datarouter-load-test provides tools to stress test different datarouter-clients with rapid `get`, `insert` and `scan`
 operations through the datarouter UI.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-load-test</artifactId>
	<version>0.0.44</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterLoadTestPluginBuilder()
		...
		.build()
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
