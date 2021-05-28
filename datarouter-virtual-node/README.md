# datarouter-virtual-node

## About

Virtual nodes are classes that implement datarouter storage interfaces by wrapping underlying physical nodes.  For example, Caching nodes wrap a persistent
node plus a cache node, making it easier to keep the cache up to date.  Or Replication nodes wrap a primary database node plus zero or more replica nodes,
making it easier to create all the nodes and then control which physical node serves a read request.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-virtual-node</artifactId>
	<version>0.0.75</version>
</dependency>
```

## Types

### Caching

A caching node is typically used to add a cache like memcached in front of persistent storage like mysql.  After creating the memcached and mysql nodes, 
pass them to the CachingNode constructor along with two booleans that should usually both be true:
- cacheOnRead: if there is a cache miss and the data is found in the backing node, then store it in the cache
- cacheOnWrite: update the cached value on every write.

For some performance critical situations, it's possible to chain the virtual CachingNodes together to build a tiered cache.

### Replication

The Replication node is typically used with databases performing their own replication to possibly multiple replicas.  Rather than building an explicit node
for each replica and accessing them individually at runtime, you pass the primaryClientId and multiple replicaClientIds to the virtual node and it will
create all the underlying nodes for you.  Then at runtime, you can use `new Config().anyDelay()` to notify the Replication node that you prefer to
read from a replica if one exists, presumably because you want to reduce load on the harder-to-scale primary database.  The current version uses a simple round-robin 
approach to pick the replica for each read which is usually fine as long as there is only one replica or the multiple replicas are keeping up on replication.

### Redundant

A redundant node wraps two writable nodes, reading from the first but writing to both.  The primary use case is migrating a table from one place to another,
usually from one database server to another, but sometimes for renaming a table on the same server or switching to a table with a different column type.  To use it
you:
- create the target node, wrap the source and target in the redundant node, and deploy the code
- run a migration job, usually using the CopyTableHandler, which backfills the source node's data to the destination node
- change the redundant node to read from the target node, and deploy to validate that everything works
- wait for a safe-rollback period to pass
- remove the source and redundant nodes, leaving only the target node

### WriteBehind

WriteBehind nodes act as a transient buffer that flushes data to the database in the background.  Simply provide them with a backing node that is actually persistent.
Their purpose is to reduce the latency of the write for the application, so it can progress through a web request or job faster, leaving the delayed flushing
work to be done by the WriteBehind nodes's internal thread.  The naming is to signify it has the opposite effect of a write-ahead log whose purpose is to safely
persist the data before completing a request.

WriteBehind nodes have mostly been superseded by the Conveyor functionality in datarouter-conveyor.  Along with more consistent monitoring and configuration, the 
conveyors allow simpler memory storage using MemoryBuffer and more reliable (and potentially multi-threaded) background flushing using the conveyor library.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
