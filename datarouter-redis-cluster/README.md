# datarouter-redis-cluster

datarouter-redis-cluster is a simple client that implements the MapStorage interface with and talks to a Redis-Cluster
 with the jedis client.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-redis-cluster</artifactId>
	<version>0.0.30</version>
</dependency>
```

## Client configuration

If the redis-cluster has auto-discovery enabled, you can use the auto discovery endpoint, or you can specify each of
 the nodes in the cluster.

There are two ways to configure the client options.

1. Configuration in a datarouter-properties file. 

Auto-discovery
```
client.myClient.type=redisCluster
client.myClient.redisCluster.clientMode=autoDiscovery
client.myClient.redisCluster.clusterEndpoint=localhost:6379
```
or

Multi-node
```
client.myClient.type=redisCluster
client.myClient.redisCluster.clientMode=multiNode
client.myClient.redisCluster.numNodes=3
client.myClient.redis.server.0=localhost:7000
client.myClient.redis.server.1=localhost:7001
client.myClient.redis.server.2=localhost:7002
```

2. Configuration in the code
You can define the client options in the code using the `RedisClusterClientOptionsBuilder` and add the
 ClientOptionsBuilder to the app's `WebappBuilder`.


Cluster auto-discovery:
```java
Properties properties = RedisClusterClientOptionsBuilder(clientId)
		.withClientMode(RedisClusterClientMode.AUTO_DISCOVERY)
		.withClusterEndpoint("localhost:6379")
		.build();
```
or
Multi-node
```java
Properties properties = RedisClusterClientOptionsBuilder(clientId)
		.withClientMode(RedisClusterClientMode.MULTI_NODE)
		.withNumServers(3)
		.withNodeIndexAndHostAndPort(0, "localhost:7000")
		.withNodeIndexAndHostAndPort(1, "localhost:7001")
		.withNodeIndexAndHostAndPort(2, "localhost:7002")
		.build();
```

or
## Local Testing
To build this module locally, add `redis-cluster.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
