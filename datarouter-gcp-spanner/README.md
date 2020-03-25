# datarouter-gcp-spanner

## About
datarouter-gcp-spanner is an implementation of [datarouter-storage](../datarouter-storage) that allows you to connect to a Google's Spanner database.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-spanner</artifactId>
	<version>0.0.24</version>
</dependency>
```

### Configuration files

#### Client configuration

There are two ways to configure the client options.

1. Configuration in a datarouter-properties file.

The following configuration file tells datarouter that the `spannerClient` client defined in the dao is a Spanner
 client that needs to connect to google's hosted spanner database.
This file needs to be located in `/etc/datarouter/config/datarouter-test-app.properties`, which is defined in the 
app's DatarouterProperties.java.

```
client.drTestSpanner.type=spanner
client.drTestSpanner.spanner.projectId=example-projectId
client.drTestSpanner.spanner.instanceId=example-instanceId
client.drTestSpanner.spanner.databaseName=test
client.drTestSpanner.spanner.credentialsLocation=/etc/datarouter/config/key.json
```

2. Configuration in the code

You can define the client options in the code using the `SpannerClientOptionsBuilder` and add the ClientOptionsBuilder
 to the app's `WebappBuilder`.

```java
Properties properties =  SpannerClientOptionsBuilder(clientId)
		.setClientOptionType(clientId)
		.withProjectId("example-projectId")
		.withInstanceId("example-instance")
		.withDatabaseName("test")
		.withCredentialsLocation("/etc/datarouter/config/key.json")
		.build();
```

#### Schema update configuration

Datarouter can create databases, tables and keep the schema up-to-date with what is defined in the code.
To activate it, you will have to add this file at `/etc/datarouter/config/schema-update.properties`.

```
schemaUpdate.enable=true
schemaUpdate.execute.addColumns=true
schemaUpdate.execute.deleteColumns=true
schemaUpdate.execute.modifyColumns=true
schemaUpdate.execute.addIndexes=true
schemaUpdate.execute.dropIndexes=true
schemaUpdate.execute.modifyEngine=true
schemaUpdate.execute.modifyCharacterSetOrCollation=true
schemaUpdate.execute.modifyRowFormat=true
schemaUpdate.execute.modifyTtl=true
schemaUpdate.execute.modifyMaxVersions=true
```

## Local Testing
To build this module locally, add the `spanner.properties` to `/etc/datarouter/test` and and `schema-update.properties`
 to `/etc/datarouter/config`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
