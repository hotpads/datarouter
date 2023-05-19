# datarouter-memory

## About
datarouter-memory is an in-memory implementation of [datarouter-storage](../datarouter-storage) nodes.  It supports
BlobStorage, MapStorage, SortedStorage, and TallyStorage.  IndexedStorage is not yet supported.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-memory</artifactId>
	<version>0.0.120</version>
</dependency>
```

## Configuration files

### Client configuration

There are two ways to configure the client options.

1. Configuration in a datarouter-properties file.

The following configuration file tells datarouter that the `memoryClient` client defined in the dao is a Memory client.
This file needs to be located in `/etc/datarouter/config/datarouter-test-app.properties`, which is defined in the app's
 DatarouterProperties.java.

```
client.myClient.type=memory
```

2. Configuration in the code

You can define the client options in the code using the `MemoryClientOptionsBuilder` and add the ClientOptionsBuilder
 to the app's `WebappBuilder`.

```java
Properties properties =  MemoryClientOptionsBuilder(clientId)
		.build();
```

## Local Testing
To build this module locally, add `memory.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
