# datarouter-memcached

datarouter-memcached is a simple client that implements the MapStorage interface with a Memcached cluster.  It can also
serve as the basis for implementing cloud provider Memcached implementations, like Amazon's Elasticache.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-memcached</artifactId>
	<version>0.0.25</version>
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

## Tally

The Tally databean holds a running tally of increments and decrements.  It's suitable for a generic counter or 
something more specific like a rate limiter.

The serialization format of the `Long tally` field is compatible with the Memcached increment operation, meaning the
resulting value will be consistent in the face of concurrent access.

To use, extend `BaseTallyDao`, specifying the Node parameters, then write and read with these DAO methods:

``` java
Long incrementAndGetCount(String key, int delta, Duration ttl, Duration timeout)
Map<String,Long> getMultiTallyCount(Collection<String> keys, Duration ttl, Duration timeout)
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuidlder`.

```java
.addWebPlugin(new DatarouterRateLimiterPluginBuilder(...)
		...
		.build()
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
