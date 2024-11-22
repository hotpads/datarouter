# datarouter-gcp-gcs

datarouter-gcp-gcs is a library that wraps the GCP SDK for Java.
It implements datarouter's `BlobStorage` interface which can be combined with datarouter's `Directory` class.
It provides operations to read, write, and delete objects, plus methods to list objects via datarouter Scanners.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-gcs</artifactId>
	<version>0.0.126</version>
</dependency>
```

## Usage

First inject the `GcsClientManager` singleton:
```java
@Inject
private GcsClientManager gcsClientManager;
```

Then obtain a `DatarouterGcsClient` instance for a particular datarouter `ClientId`:
```
DatarouterGcsClient gcsClient = gcsClientManager.getClient(MyClientIds.MY_CLIENT_ID);
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
