# datarouter-logging

datarouter-logging extends log4j2 to provide logging configuration using type-safe Java, instead of XML configuration files. It also provides an easy way to modify logging levels and appenders at runtime.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-logging</artifactId>
	<version>0.0.119</version>
</dependency>
```

## Usage

### Default usage

By default, datarouter-logging defines an appender for the standard output, and sends everything with level WARN to it. To log, simply define a slf4j logger and use it like that:

```java
package io.datarouter.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args){
		logger.warn("sent to stdout");
		logger.info("not sent to stdout");
	}
}
```

### Static logging configuration

To define a custom static logging configuration, you will need to extend `BaseLog4j2Configuration`. In the constructor of your implementation, you can define appenders and logger configurations, or also use `registerParent` to inherit a configuration.

```java
package io.datarouter.example;
//imports
public class ExampleLog4j2Configuration extends BaseLog4j2Configuration{
	public ExampleLog4j2Configuration(){
		//Register the default configuration as a parent to inherit the default stdout appender
		registerParent(DatarouterLog4j2Configuration.class);
		
		//Retrieve the stdout appender
		Appender console = getAppender(DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME);
		
		//Set the logger level of the current package to INFO
		addLoggerConfig(getClass().getPackage().getName(), Level.INFO, false, console);
	}
}
```

You will need to register this configuration class by creating a file in `src/main/resources` with the `.datarouter-logging` extension that contains the full name of the class.

`src/main/resources/log4j2.datarouter-logging`
```
io.datarouter.example.ExampleLog4j2Configuration
```

### Runtime logging configuration

You can use `Log4j2Configurator` to update the configuration of your loggers at runtime.

```java
package io.datarouter.example;
//imports
public class Main{
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args){
		logger.debug("not logged");

		//Instantiate Log4j2Configurator
		Log4j2Configurator configurator = new Log4j2Configurator();
		String console = DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME;
		
		//Set the level of the logger for the current class to DEBUG
		configurator.updateOrCreateLoggerConfig(Main.class.getPackage(), Level.DEBUG, false, console);

		logger.debug("logged");
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
