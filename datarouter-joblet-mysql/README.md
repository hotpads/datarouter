# datarouter-joblet-mysql

`Datarouter-joblet-mysql` holds the configuration that allows `datarouter-mysql` to be used as the queue backing
 system for `datarouter-joblet`.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-joblet-mysql</artifactId>
	<version>0.0.85</version>
</dependency>
```

## Installation with Datarouter

In the `DatarouterJobletWebappConfigBuilder`, specify the following selector types:
```java
.addJobletSelector(
		JobletQueueMechanism.JDBC_LOCK_FOR_UPDATE.getPersistentString(),
		MysqlLockForUpdateJobletRequestSelector.class)
.addJobletSelector(
		JobletQueueMechanism.JDBC_UPDATE_AND_SCAN.getPersistentString(),
		MysqlUpdateAndScanJobletRequestSelector.class)
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
