# datarouter-service-config

datarouter-service-config is a small tool that allows an app to publish information about itself to external services.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-service-config</artifactId>
	<version>0.0.70</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterServiceConfigPluginBuilder(...)
		...
		.build()
```

## Usage
This feature can be useful for tracking configuration information across multiple services.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
