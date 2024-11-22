# datarouter-aws-rds

datarouter-aws-rds provides tools to monitor amazon's relational-database-services configurations for the application.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-elb</artifactId>
	<version>0.0.126</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterAwsRdsluginBuilder(...)
	...
	.build())
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
