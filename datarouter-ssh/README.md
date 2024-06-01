# datarouter-ssh

datarouter-ssh utilizes the jsch library to handle ssh connections.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-ssh</artifactId>
	<version>0.0.125</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterSshPluginBuilder()
		...
		.build()
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
