# datarouter-memcached

datarouter-memcached is a simple client that implements the MapStorage interface with a Memcached cluster.  It can also
serve as the basis for implementing cloud provider Memcached implementations, like Amazon's Elasticache.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-memcached</artifactId>
	<version>0.0.76</version>
</dependency>
```

## Client configuration

There are two ways to configure the client options. 

1. Configuration in a datarouter-properties file. 

```
client.myClient.type=memcached
client.myClient.memcached.numServers=1
client.myClient.memcached.server.0=localhost:11211
```

2. Configuration in the code
You can define the client options in the code using the `MemcachedClientOptionsBuilder` and add the ClientOptionsBuilder to the app's `WebappBuilder`. 

```java
Properties properties =  MemcachedClientOptionsBuilder(clientId)
		.withNumServers(1)
		.withServerIndexAndInetSocketAddress(0, "localhost:11211")
		.build();
```

## Monitoring

datarouter-memcached includes a monitoring page to view statistics about client operations (`get_hits`, `get_misses`, 
`evictions`, etc) on each server.  Navigate to it by selecting your memcached client name on the datarouter homepage. 

## Local Testing
To build this module locally, add `memcached.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
