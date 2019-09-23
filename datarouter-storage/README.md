# datarouter-storage

Datarouter Storage holds the classes and interfaces that let you persist your Databeans to different datastores via
runtime Client implementations such as datarouter-mysql.

## Links to source code with comments

- [ClientManager](./src/main/java/io/datarouter/storage/client/ClientManager.java)
- [Node](./src/main/java/io/datarouter/storage/node/Node.java)
- [PhysicalNode](./src/main/java/io/datarouter/storage/node/type/physical/PhysicalNode.java)
- [Router](./src/main/java/io/datarouter/storage/router/Router.java)

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


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
