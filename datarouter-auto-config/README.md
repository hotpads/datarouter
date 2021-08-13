# datarouter-auto-config

datarouter-auto-config is a tool that can be used to do any type of automatic configuration when the application is
 running.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-auto-config</artifactId>
	<version>0.0.85</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWeblugin(new DatarouterAutoConfigPluginBuilder(...)
		...
		.build()
```

## Usage

The auto-config tool can be used as an AppListener or called through a Handler. 

The `AppListener` can be specified through the plugin builder and needs to implement `AutoConfigListener`. 

The `AutoConfigHandler` injects a collection of `AutoConfigClasses` (which are added through the plugin-builder) and
 is triggered by hitting the autoConfg endpoint. Each `AutoConfigClass` returns a string response of what was configured
 and is displayed on the output.

One example to use datarouter-auto-config could be to populate databases for development environments. Instead of
 each development machine importing a data dump manually, which could be outdated or have an incorrect schema, the
 insert databeans could be built in the code.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
