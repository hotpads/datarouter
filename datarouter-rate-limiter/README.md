# datarouter-rate-limiter

datarouter-rate-limiter keeps a rolling window of counters for specific actions so that they can be rejected after a quota.
Rate-limiters utilize the `TallyStorage` interface, which is implemented by datarouter-memcached and datarouter-redis.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-rate-limiter</artifactId>
	<version>0.0.113</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterRateLimiterPluginBuilder(...)
		...
		.build()
```

## Tally

The Tally databean holds a running tally of increments and decrements.  It's suitable for a generic counter or
 something more specific like a rate limiter.

The serialization format of the `Long tally` field is compatible with the Memcached or Redis increment operation,
 meaning the resulting value will be consistent in the face of concurrent access.

To use, extend `BaseTallyDao`, specifying the Node parameters, then write and read with these DAO methods:

``` java
Long incrementAndGetCount(String key, int delta, Duration ttl, Duration timeout)
Map<String,Long> getMultiTallyCount(Collection<String> keys, Duration ttl, Duration timeout)
```

## Local Testing
To build this module locally, add `rate-limiter.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
