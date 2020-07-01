# datarouter-hbase

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-hbase</artifactId>
	<version>0.0.38</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterHbasePluginBuilder(...)
		...
		.build()
```

## About

The datarouter-hbase module holds a client that implements Datarouter's MapStorage and SortedStorage interfaces.  It 
also has utilities to help you add secondary indexes in the form of additional tables.

Google's Bigtable database, which HBase is designed after, also supports access via the HBase Java client library.
Therefore another Datarouter module, datarouter-bigtable, extends datarouter-hbase to provide access to Bigtable.

## Data Model

HBase has a binary API, where you must convert your data into byte arrays before storing them.  Everything inside HBase
is sorted by lexicographically comparing the byte arrays, so it is lacking some of the character set and collation
features of traditional relational databases.

If a Databean has 3 fields in the PrimaryKey and 5 Fields outside the PrimaryKey, then you will end up with 5 HBase
Cells per Databean.  The values of the 3 PK fields will be repeated as the row key for each of the 5 cells.  HBase
internally supports Cell "encoding" that can reduce the disk and memory footprint of those repeated keys.

#### HBase Row

Each Databean's PrimaryKey becomes an HBase "row" key.  It's created by converting each PK Field value into a byte[]
and appending them together.

Fixed-width fields like ints and doubles have no separator characters, and their first
bits are flipped so that the rows are sorted the same way inside HBase (as byte[]'s) vs outside as normal Java fields.

Strings are converted to bytes using the UTF-8 encoding.  Variable-width Fields like Strings are terminated with a '0' 
byte to ensure that a shorter String sorts before a longer one if otherwise equal.  There is no trailing zero byte if 
the PK ends with a String.

#### HBase ColumnFamily

Datarouter-hbase uses only one ColumnFamily per table, so it's similar to relational databases.  We hard-code the name
of the ColumnFamily to 'a'.

#### HBase ColumnQualifier

For each Field in the Databean that is not in the PrimaryKey (non-PK Fields), an HBase Cell is created.  The ColumnQualifier
is the name of the Field that is specified in the FieldKey.

#### HBase Value

The Cell's value is simply the byte[] encoding of the Field's value.  If a Field has a null value, datarouter-hbase
will create a Delete operation for that Field to ensure any previous values were erased, but not store any new Cell
after compaction has occurred.  Note that because of this behavior, if you're removing a Field from a Databean,
it's important to nullify that Field in existing Databeans before removing their Field definition from the Fielder, 
otherwise you will have ghost Fields in the database.

## Partitioning

A built in partitioning mechanism calculates a partition based on the EntityKey of each Databean.  The default
DefaultEntityPartitioner will transparently hash the EntityKey into 16 buckets, convert it to a byte, and prepend the byte to the
beginning of each row key.  Therefore all Databeans in a single Entity are located together on disk for efficient retrieval.

When scanning the table, the client will decide to scan a single partition if the full EntityKey is specified as a prefix,
or scan all partitions if the full EntityKey isn't specified.  When scanning all partitions, the results are merged
using a CollatingScanner so they arrive in PrimaryKey ordering.

We have found the default 16 partitions works well for small or large tables, though it is configurable by adding a new
implementation of EntityPartitioner.  Adding more partitions will scatter the writes to more nodes, potentially increasing write
throughput and avoiding a hotspot on a single node.  It especially enables faster appending to the end
of the table compared to appending to a table with a single partition.

Adding partitions does come at a cost though.  When scanning without knowing the full EntityKey,
more scans will need to be executed to cover all the partitions and merge the results, and there may be overhead of 
fetching extra Databeans from the last batch of each scanner.

## Parallel Scanners

By default, scanning the partitions is done in an ExecutorService with each partition scanner running in its own thread.  This
can speed up fetching of many rows, with over 100,000 Databeans per second being commonplace for multi-partition scans.
Keep in mind this parallel scanning does use more overall CPU during the scan than a single-threaded scan.

## Schema Updates

When the datarouter-hbase client is initialized, it will run a schema-update mechanism that compares the state of the 
database with the Databeans that are registered in code.  Any missing tables will be created using options specified
in the Fielder, or else defaults.

New tables will be pre-split with a region for each partition which immediately enables high throughput writes and reads
without waiting for enough data to trigger normal splits.

Defaults:
- `maxFileSize`: 4 GiB
- `memstoreFlushSize`: 256 MiB
- `maxVersions`: 1
- `bloomFilterType`: NONE
- `dataBlockEncoding`: FAST_DIFF
- `compressionType`: GZip
- `ttl`: none

Example custom TTL, specified inside the Fielder:
```java
@Override
public void configure(){
	addOption(new TtlFielderConfig(Duration.ofDays(21)));
}
```

## Region Balancing

If configured, an HBaseRegionBalancerJob will run every 10 minutes in the background and instruct HBase to move
regions to given servers if not already there.  This can be beneficial because Datarouter knows more about the layout 
of the data than HBase, particularly the which partition a region belongs to.
  
A ConsistentHashBalancer is used to spread regions among servers, to minimize disruption
as nodes are added or removed.  If a node is temporarily removed its old regions should return to it, increasing
data locality and reducing network usage and latency.

## Compaction Scheduling

If configured, the HBaseCompactionJob will run every 10 minutes and trigger a major compaction on a few regions that
are selected using a simple hash/modulo calculation.  It
has a default compaction period of 4 days, meaning every region in the cluster is triggered every 4 days.

This can be addded to the plugin builder. 
To configure, create an implementation of HBaseCompactionInfo and register it in the DatarouterHbasePluginBuilder:

The defaults can be changed by overriding these methods in HBaseCompactionInfo:
```java
default Duration getCompactionTriggerPeriod(){
	return Duration.ofMinutes(10);
}

default Duration getCompactionPeriod(@SuppressWarnings("unused") DrRegionInfo<?> regionInfo){
	return Duration.ofDays(4);
}
```

The HBaseCompactionJob can be beneficial if you want to ensure your regions don't sit uncompacted for too long, 
especially when they have been relocated to a new server.  Running a major compaction will result in a local copy of the
data on that machine which is more efficient for reads.

## Wide Nodes

An old, but still experimental, feature called Wide Nodes can place all Databeans with the same EntityKey into a single 
HBase row.  This can speed up reads by fetching all databeans with a single HBase Get operation.  Further, if specified,
it can group multiple types of databeans into the same HBase row if they participate in the same Entity (have the same
EntityKey).

Wide Nodes aren't recommended unless under very controlled circumstances.

Here are some potential problems:
- If the number of databeans in the Entity
grows large (several hundred megabytes), it may cause problems with the RPC mechanism and cause memory errors.  BigTable
limits row sizes to under a gigabyte to prevent some of these problems.
- HBase doesn't restrict the size of a
row when writing, but if it grows to multiple gigabytes then HBase may fail to compact it, leaving the cluster in a bad
state.
- If multiple Databean types are being stored in the Entity and many consecutive Entities don't have any of a particular
Databean, then a scan for that sparse Databean will cause the scanner to "skip rows", processing lots of data on the 
RegionServer but not returning it to the client, resulting in unpredictable latencies and aggressive CPU and disk usage.

## Counters

Counters for client level events can be found with prefix `Datarouter client hbase [clientName]`.

Counters for node level events can be found with prefix `Datarouter node hbase [clientName] [nodeName]`

Datarouter-hbase also supports more intricate counters to track events at the server and region levels.  Prefixes include:
- `Datarouter client-server rows [clientName] [serverName]`
- `Datarouter client-server-table rows [clientName] [serverName] [tableName]`
- `Datarouter client-server-table-region rows [clientName] [serverName] [tableName] [regionId]`
- `Datarouter client-server-table-op rows [clientName] [serverName] [tableName] [operation]`
- `Datarouter client-table-server rows [clientName] [tableName] [serverName]`
- `Datarouter client-table-server-op rows [clientName] [tableName] [serverName] [operation]`

## Management UI

HBase exposes a thick Java client that we use to do things like creating tables.  Datarouter-hbase takes advantage of 
that to add some administrative features to the UI.  Access them by selecting an HBase client on the webapp's
`/datarouter` homepage.

- **Overview page**: lists tables with per-table setting information and links to other pages
- **Regions page**: lists regions for a single table with statistics
    - Highlights regions that are on the wrong server according to the HBaseRegionBalancer
    - Options to move regions, trigger compaction, flush memstores, etc
    - Shows next scheduled compaction time for the HBaseCompactionJob
- **Servers page**: lists servers with region count, storefile count, etc
- **Settings page**: UI for editing Table and Column Family settings such as MAX_FILESIZE or TTL

## Client configuration

There are two ways to configure the client options. 

1. Configuration in a datarouter-properties file. 

```
client.myClient.type=hbase
client.myClient.hbase.zookeeper.quorum=hbase.docker
```

2. Configuration in the code
You can define the client options in the code using the `HBaseClientOptionsBuilder` and add the ClientOptionsBuilder to the app's `WebappBuilder`. 

```java
Properties properties =  HBaseClientOptionsBuilder(clientId)
		.withZookeeperQuorum("hbase.docker")
		.build();
```
#### Schema update configuration

Datarouter can create databases, tables and keep the schema up-to-date with what is defined in the code.
There are two ways to configure the schema update options.

1. Configuration in a schema-update.properties file.

To activate it, you will have to add this file at `/etc/datarouter/config/schema-update.properties`.

```
schemaUpdate.enable=true
schemaUpdate.execute.createTables=true
schemaUpdate.execute.modifyTtl=true
schemaUpdate.execute.modifyMaxVersions=true
```

2. Configuration in the code

You can define the schema update options in the code using the `SchemaUpdateOptionsBuilder` and add the implementation
of `SchemaUpdateOptionsFactory` to the app's `WebappBuilder`.

```java
Properties properties = new SchemaUpdateOptionsBuilder(true)
		.enableSchemaUpdateExecuteCreateTables()
		.enableSchemaUpdateExecuteModifyTtl()
		.enableSchemaUpdateExecuteModifyMaxVersions()
		.build();
```

On production environments, it is recommended to use `schemaUpdate.print` instead of `schemaUpdate.execute`. The ALTER TABLE statements will be logged and emailed instead of executed.

## Local Testing
To build this module locally, add `hbase.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
