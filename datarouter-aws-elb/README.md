# datarouter-aws-elb

datarouter-aws-elb provides tools to monitor amazon's elastic-load-balancer configurations for the application.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-elb</artifactId>
	<version>0.0.77</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterAwsElbPlugin())
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
