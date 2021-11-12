# datarouter-redis

datarouter-redis is a simple client that implements the MapStorage interface with and talks to a Redis instance or Redis cluster with
 the lettuce.io client.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-redis</artifactId>
	<version>0.0.95</version>
</dependency>
```

## Client configuration

The `clientMode` needs to be specified. There are three options, `autoDiscovery`, `multiNode`, and `standard`. 

If you are using a single redis instance, then use `standard`. If you are using a clustered redis, then use either `autoDiscovery` or `multiNode` based on your configuration. 

### Standard redis

1. Configuration in a datarouter-properties file.

```
client.myClient.type=redis
client.myClient.redis.clientMode=standard
client.myClient.redis.endpoint=localhost:6379
```

2. Configuration in the code
You can define the client options in the code using the `RedisClientOptionsBuilder` and add the ClientOptionsBuilder
 to the app's `WebappBuilder`.

```java
Properties properties = RedisClientOptionsBuilder(clientId)
		.withClientMode(RedisClientMode.STANDARD)
		.withEndpoint("localhost:6379")
		.build();
```

### Redis-Cluster
1. Configuration in a datarouter-properties file. 

Auto-discovery
```
client.myClient.type=redis
client.myClient.redis.clientMode=autoDiscovery
client.myClient.redis.endpoint=localhost:6379
```
or

Multi-node
```
client.myClient.type=redis
client.myClient.redis.clientMode=multiNode
client.myClient.redis.numNodes=3
client.myClient.redis.server.0=localhost:7000
client.myClient.redis.server.1=localhost:7001
client.myClient.redis.server.2=localhost:7002
```

2. Configuration in the code
You can define the client options in the code using the `RedisClientOptionsBuilder` and add the
 ClientOptionsBuilder to the app's `WebappBuilder`.


Cluster auto-discovery:
```java
Properties properties = RedisClientOptionsBuilder(clientId)
		.withClientMode(RedisClientMode.AUTO_DISCOVERY)
		.withEndpoint("localhost:6379")
		.build();
```
or
Multi-node
```java
Properties properties = RedisClientOptionsBuilder(clientId)
		.withClientMode(RedisClientMode.MULTI_NODE)
		.withNumServers(3)
		.withNodeIndexAndHostAndPort(0, "localhost:7000")
		.withNodeIndexAndHostAndPort(1, "localhost:7001")
		.withNodeIndexAndHostAndPort(2, "localhost:7002")
		.build();
```

## Local Testing
To build this module locally, add `redis.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
