# datarouter-logger-config

datarouter-logger-config provides an easy way to control output logging through the UI. 

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-logger-config</artifactId>
	<version>0.0.76</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterLoggerConfigPluginBuilder(...)
		...
		.build()
```

## Usage
By default, only loggers configured at the WARN level or higher will be appended to the output, but through the UI you can configure 
other levels and control specific logger classes. 

You can add a logger through the UI with the following fields
* `name` - The canonical name of the class you want to view the output for
* `ttl` - The logger will expire after the given ttl. No ttl means that the logger won't expire. 
* `logger-level` - ALL, TRACE, DEBUG, INFO, WAWN, FATAL, ERROR, OFF
* `appender` - Console, SchemaUpdate, etc...

Appenders
* FileAppender - Writes logger statements to a file
* ConsoleAppender - Writes logger statements to tomcat's console output

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
