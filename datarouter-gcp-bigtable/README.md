# datarouter-gcp-bigtable

## About
datarouter-gcp-bigtable uses the native bigtable java client to talk to Google's Bigtable database

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterBigtablePlugin())
```

## Local Testing
To build this module locally, add `bigtable.properties` to `/etc/datarouter/test`.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-bigtable</artifactId>
	<version>0.0.126</version>
</dependency>
```
## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
