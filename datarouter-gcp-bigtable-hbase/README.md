# datarouter-gcp-bigtable-hbase

## About
datarouter-gcp-bigtable-hbase uses the HBase client to talk to Google's Bigtable database

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-bigtable-hbase</artifactId>
	<version>0.0.107</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterBigTablePlugin())
```

## Local Testing
To build this module locally, add `bigtable.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
