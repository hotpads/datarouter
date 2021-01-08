# datarouter-gcp-spanner

## About
datarouter-gcp-spanner is an implementation of [datarouter-storage](../datarouter-storage) that allows you to connect to a Google's Spanner database.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-spanner</artifactId>
	<version>0.0.59</version>
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
There are two ways to configure the schema update options.

1. Configuration in a schema-update.properties file.

To activate it, you will have to add this file at `/etc/datarouter/config/schema-update.properties`.

```
schemaUpdate.enable=true
schemaUpdate.execute.createDatabases=true
schemaUpdate.execute.createTables=true
schemaUpdate.execute.addColumns=true
schemaUpdate.execute.deleteColumns=true
schemaUpdate.execute.modifyColumns=true
schemaUpdate.execute.addIndexes=true
schemaUpdate.execute.dropIndexes=true
```

2. Configuration in the code

You can define the schema update options in the code using the `SchemaUpdateOptionsBuilder` and add the implementation
of `SchemaUpdateOptionsFactory` to the app's `WebappBuilder`.

```java
Properties properties = new SchemaUpdateOptionsBuilder(true)
		.enableSchemaUpdateExecuteCreateDatabases()
		.enableSchemaUpdateExecuteCreateTables()
		.enableSchemaUpdateExecuteAddColumns()
		.enableSchemaUpdateExecuteDeleteColumns()
		.enableSchemaUpdateExecuteModifyColumns()
		.enableSchemaUpdateExecuteAddIndexes()
		.enableSchemaUpdateExecuteDropIndexes()
		.build();
```

On production environments, it is recommended to use `schemaUpdate.print` instead of `schemaUpdate.execute`. The ALTER TABLE statements will be logged and emailed instead of executed.

## Local Testing
To build this module locally, add `spanner.properties` to `/etc/datarouter/test` and add `schema-update.properties`
 to `/etc/datarouter/config`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
