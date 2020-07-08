# datarouter-redis

datarouter-redis is a simple client that implements the MapStorage interface with and talks to a Redis instance with
 the jedis client.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-redis</artifactId>
	<version>0.0.39</version>
</dependency>
```

## Client configuration

There are two ways to configure the client options.

1. Configuration in a datarouter-properties file.

```
client.myClient.type=redis
client.myClient.redis.endpoint=localhost:6379
```

2. Configuration in the code
You can define the client options in the code using the `RedisClientOptionsBuilder` and add the ClientOptionsBuilder
 to the app's `WebappBuilder`.

```java
Properties properties = RedisClientOptionsBuilder(clientId)
		.withEndpoint("localhost:6379")
		.build();
```

## Local Testing
To build this module locally, add `redis.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
