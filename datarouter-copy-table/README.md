# datarouter-copy-table

## About
datarouter-copy-table is tool that allows you to easily migrate tables of any size from one datastore to another.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-copy-table</artifactId>
	<version>0.0.87</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobletPlugin(new DatarouterCopyTablePluginBuilder()
		...
		.build())
```

## Usage

The copy table relies on two tables, where the databean fielders are the same. Its recommended to use a
 redundant node so data isn't lost during the migration.

There are two ways to use the copy-table tool, with a single thread or with joblets. Tables can be copied
 using either of the two methods through the datarouter ui.

### Single Thread
The single thread method is useful for small tables.

### Joblets
The joblet method is useful for very large tables and relies on the samples recorded in `TableSample`.
 A joblet is created for each sample.

## Custom Scanning Configurations

For advance customization, you can add CopyTableConfigurations, which can be added to the plugin builder.

```java
public class ExampleCopyTableConfiguration extends BaseCopyTableConfiguration{

	public ExampleCopyTableConfiguration(){
		registerFilter("customConfigForExampleDatabean",
				ExampleCopyTableConfiguration::customConfigForExampleDatabean);
	}

	private static boolean customConfigForExampleDatabean(ExampleDatabean databean){
		if(databean.getKey().getDate().getYear() < 2019){
			return false;
		}
		return true;
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
