# datarouter-data-export

Datarouter-data-export exports a table to BlobStorage (Filesystem, GCS, or S3) and then imports it somewhere else.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-data-export</artifactId>
	<version>0.0.119</version>
</dependency>
```

## Installation

You can install this module by adding its plugin to the `WebappBuilder`.
You'll need to pass it the `ClientId`s that it's allowed to export/import, 
  and a `DatarouterDataExportDirectorySupplier` that specifies a `Directory` where the data is stored.

```java
addWebPlugin(new DatarouterDataExportPluginBuilder(
		myClientIds,
		MyDirectorySupplier.class)
		.build());
```

## Usage

To export, select a `Node` from the datarouter UI that implements `SortedStorage` and click `export`.
When the export is complete, you'll receive an email with a link to optionally import the data to `localhost`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
