# datarouter-nodewatch

## About
Datarouter-nodewatch is a tool to count tables in a configurable manner. 

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-nodewatch</artifactId>
	<version>0.0.78</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobletPlugin(new DatarouterNodewatchPluginBuilder(...)
		...
		// add clients that should be counted
		.addNodewatchClientId(countableClient)
		.build()
```

## Features

### Table Count

Each client that should be counted needs to be added to the `DatarouterNodewatchPluginBuilder` when installed in the 
`WebappBuilder` with the `.addNodewatchClientId(clientId)`.

The `TableCountJob` runs multiple times a day and recomputes each of the recorded `TableRowSample`s for each table 
based on the number of samples saved. Samples are staggered so each is counted once per week, with some exceptions 
like the last sample being recounted more frequently. Each `TableCount` is saved with the date it was created and 
can be viewed on a graph on the datarouter page. The latest `TableCount` is saved in the `LatestTableCount` table 
for simple access and viewing.

Datastores or clients you don't want to count
* caching datastores
* queue storage datastores
* reader instances for mysql


### Table Row Sample

Some tables can grow really large and can be taxing on the server and the database to count each day. The 
`TableSamplerJob` helps with that by storing a multiple samples of each table in the `TableRowSample` table. The 
default sample size is 1,000,000, but can be configured in the dao as a node option. Each sample knows where it 
lies in the table and is constantly readjusting since the tables are constantly being written and deleted from. 
The `TableSamplerJob` runs every 10 minutes and creates joblets which attempt to recompute the samples so that 
when the `TableCountJob` runs, the counts are accurate.


### Latest Table Count

The `LatestTableCount` is stored for each table. This is useful when trying to get a rough idea of how small or large 
table is. However, this is not as accurate as a `countKeys` would be since, the count is based off of the `TableCount`. 
You can view all of the latest counts grouped by client on the datarouter UI. 


### Alerting

`TableSizeMonitoringJob` runs everyday and does two checks. 

1. Stale Entries - Datarouter is aware of all of its nodes. When a node is deleted from the code, there's still an 
entry for it in the nodewatch tables. This alert helps keep the databases clean by notifying the app's administrators 
on the tables that should be deleted. 

2. Table Sizes - Each table can configure a maxThreshold and a percentageChanged. If a table surpasses its specified 
threshold, or increases by an unusual amount, an email will be sent to the app's administrators. 


### Specific Table Configurations

When defining a node in a Dao, you can configure custom table options through the node factory and view them on the 
`Custom Table Configurations` datarouter page. 

* `setSampleMaxThreshold(long)`
* `setSamplerPercentageChangedAlertEnabled(boolean)`
* `setSamplerThresholdAlertEnabled(boolean)`
* `setSamplerInterval(long)`
* `setSamplerEnabled(boolean)`
* `setSamplerSize(long)`

Example from `DatarouterJobletDataDao`

```java
@Inject
public DatarouterJobletDataDao(Datarouter datarouter, NodeFactory nodeFactory, DatarouterJobletDataDaoParams params){
	super(datarouter);
	node = nodeFactory.create(params.clientId, JobletData::new, JobletDataFielder::new)
			.setSampleMaxThreshold(1_000_000L)
			.setSamplerPercentageChangedAlertEnabled(false)
			.setSamplerThresholdAlertEnabled(false)
			.buildAndRegister();
}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
