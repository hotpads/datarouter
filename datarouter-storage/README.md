# datarouter-storage

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-storage</artifactId>
	<version>0.0.115</version>
</dependency>
```

## Usage

Datarouter Storage holds the classes and interfaces that let you persist your Databeans to different datastores via
 runtime Client implementations such as datarouter-mysql, datarouter-aws-sqs, datarouter-memcached,
 datarouter-redis, etc.

## Links to source code with comments

- [ClientManager](./src/main/java/io/datarouter/storage/client/ClientManager.java)
- [Node](./src/main/java/io/datarouter/storage/node/Node.java)
- [PhysicalNode](./src/main/java/io/datarouter/storage/node/type/physical/PhysicalNode.java)
- [Dao](./src/main/java/io/datarouter/storage/dao/Dao.java)

- [Datarouter](./src/main/java/io/datarouter/storage/Datarouter.java)
- [DatarouterClients](./src/main/java/io/datarouter/storage/client/DatarouterClients.java)
- [DatarouterNodes](./src/main/java/io/datarouter/storage/node/DatarouterNodes.java)

- [StorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/StorageWriter.java)

- [MapStorageReader](./src/main/java/io/datarouter/storage/node/op/raw/read/MapStorageReader.java)
- [MapStorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/MapStorageWriter.java)

- [SortedStorageReader](./src/main/java/io/datarouter/storage/node/op/raw/read/SortedStorageReader.java)
- [SortedStorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/SortedStorageWriter.java)

- [QueueStorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/QueueStorageWriter.java)
- [QueueStorageReader](./src/main/java/io/datarouter/storage/node/op/raw/read/QueueStorageReader.java)
- [QueueStorage](./src/main/java/io/datarouter/storage/node/op/raw/QueueStorage.java)

- [MultiIndexReader](./src/main/java/io/datarouter/storage/node/op/index/MultiIndexReader.java)

- [IndexedStorageReader](./src/main/java/io/datarouter/storage/node/op/raw/read/IndexedStorageReader.java)
- [IndexedStorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/IndexedStorageWriter.java)

- [TallyStorageReader](./src/main/java/io/datarouter/storage/node/op/raw/read/TallyStorageReader.java)
- [TallyStorageWriter](./src/main/java/io/datarouter/storage/node/op/raw/write/TallyStorageWriter.java)

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
