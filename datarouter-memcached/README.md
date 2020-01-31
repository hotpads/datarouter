# datarouter-memcached

datarouter-memcached is a simple client that implements the MapStorage interface with a Memcached cluster.  It can also
serve as the basis for implementing cloud provider Memcached implementations, like Amazon's Elasticache.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-memcached</artifactId>
	<version>0.0.16</version>
</dependency>
```

## Client configuration

To configure a client named `myClient` on localhost, your datarouter properties file would contain:

```
client.myClient.type=memcached
client.myClient.memcached.numServers=1
client.myClient.memcached.server.0=localhost:11211
```

## Monitoring

datarouter-memcached includes a monitoring page to view statistics about client operations (`get_hits`, `get_misses`, 
`evictions`, etc) on each server.  Navigate to it by selecting your memcached client name on the `/datarouter` homepage. 

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

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
