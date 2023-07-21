# Datarouter

## Overview

Datarouter is a Java database abstraction layer 
  that gives developers a simple interface to a variety of databases. 
It sits on top of other database client libraries and encourages access through a standard set of methods 
  allowing an application to mix and match databases 
  while staying independent of any particular database product. 
The application is programmed against Datarouter’s interfaces 
  while JDBC, Memcached, HBase, etc are left as swappable dependencies.
Developers can also choose to code directly against a specific database in performance critical situations 
  or when using unique features of a particular product, 
  still leaving the vast majority of the code in a portable state.

The first goal of Datarouter is to encourage type-safety and early error detection in all layers of your application
  so that it remains easy to refactor as it grows. 
Waiting for the results of an integration or even unit test suite is too slow 
  for the frequent refactoring that's needed to quickly evolve an application beyond the original design goals. 
We want to catch errors at compile time, 
  particularly when the Eclipse incremental compiler runs, 
  and give your IDE the ability to automatically change large amounts of code.

The second goal is to encourage safe interactions with your database, 
  making it easy to fetch data in a way that won't trigger expensive queries 
  and making it harder to accidentally cause large tablescans or joins that can affect the overall application health.

The third goal is the portability mentioned above. 
Knowing your application is portable from the get-go means you can jump in and start writing code 
  without spending a lot of time debating which database you'll use 
  or which cloud provider you'll be tied to. 
A program can start out with a traditional relational database like MySQL, 
  and as certain tables grow they can be offloaded to more scalable systems 
  after trivial code changes. 
Or you might run on multiple systems at the same time, 
  such as a multi-cloud application that uses Aurora, SQS, and Elasticache Memcached in Amazon's cloud 
  while also using Cloud SQL, Cloud Pub/Sub and Memcached in Google's cloud.

For more information about the storage design, see the [datarouter-storage README](datarouter-storage/README.md).

Datarouter includes a web framework (datarouter-web) that aims to be lightweight
  and to rely on Java (not XML, JSON, or plain text) 
  for configuring things like web request handler mappings. 
Additional modules like datarouter-exception, datarouter-job, datarouter-trace, etc 
  include many building blocks of a web app 
  like log management, authentication, cron triggering, exception recording, counters, etc. 
Combining the storage and web frameworks and the added utils allows for building portable web apps 
  that are easy to move between different database engines. 
For example, you could record exception stack traces to MySQL on your laptop, 
  to HBase in your datacenter, 
  DynamoDB when running on AWS, 
  or Bigtable when running on Google Cloud.

## Lightweight utils
These libraries can be used individually without importing the whole framework.
- [scanner](datarouter-scanner/README.md)
  - Iterator library with features not present in `Stream`
- [path-node](datarouter-path-node/README.md)
  - Data structure for defining a tree of strings for URLs and Filesystems
- [enum](datarouter-enum/README.md)
  - Enum-related utils, particularly `MappedEnum`
- [types](datarouter-types/README.md)
  - A few data types pre-configured for Json and Databeans
- [bytes](datarouter-bytes/README.md)
  - Low level utils for working with bytes
  - Codec interfaces for encoding and decoding objects
- [binary-dto](datarouter-binary-dto/README.md)
  - A pure-java binary serialization format with sortable encoding
- [util](datarouter-util/README.md)
  - Collection of miscellaneous utils

## Core framework
### Data
- [model](datarouter-model/README.md)
  - Core storage classes like PrimaryKey, Databean, and Node
- [storage](datarouter-storage/README.md)
  - Generic code shared by storage client implementations
- [virtual-node](datarouter-virtual-node/README.md)
  - Caching, Redundant, and Replication nodes

### Web
- [http-client](datarouter-http-client/README.md)
  - Wrapper around Apache Http Client
  - Includes many foundational datarouter web related classes
- [web](datarouter-web/README.md)
- [auth](datarouter-auth/README.md)
  - User and Role system for web applications
- [websocket](datarouter-websocket/README.md)
  - Makes websockets easier to use

### Job
- [conveyor](datarouter-conveyor/README.md)
  - Run a number of threads in loop, usually consuming a queue
- [job](datarouter-job/README.md)
  - Scheduled tasks via cron triggers
- [joblet](datarouter-joblet/README.md)
  - Parallelized tasks stored in a queue+database for enhanced monitoring, restarting, etc
- [joblet-mysql](datarouter-joblet-mysql/README.md)
  - Low-volume Joblet locking mechanism implemented in MySql

## Additional Features
### Server configuration features
- [auto-config](datarouter-auto-config/README.md)
  - Run a set of tasks to pre-configure a new service, commonly in dev and staging
- [cluster-setting](datarouter-cluster-setting/README.md)
  - Java-defined configurations per environment/serverType/etc that can be overridden at runtime
- [logger-config](datarouter-logger-config/README.md)
  - Dynamic configuration of log levels
- [rate-limiter](datarouter-rate-limiter/README.md)
  - Cache or memory-backed rate limiter with time windows
- [secret](datarouter-secret/README.md)
  - Interfaces for secret storage
- [secret-web](datarouter-secret-web/README.md)
  - UI for editing secrets
- [service-config](datarouter-service-config/README.md)
  - Collects and publishes high-level information about running services

### Server monitoring features
- [instrumentation](datarouter-instrumentation/README.md)
  - Interfaces that datarouter uses to emit internal data
  - The interfaces need to be implemented separately
- [changelog](datarouter-changelog/README.md)
  - Log of administrative actions in a service
- [exception](datarouter-exception/README.md)
  - Collects and publishes exception details defined by datarouter-instrumentation
- [load-test](datarouter-load-test/README.md)
  - Simulate database traffic
- [metric](datarouter-metric/README.md)
  - Collects and publishes counts and metrics defined by datarouter-instrumentation
- [task-tracker](datarouter-task-tracker/README.md)
  - Collect stats from a running task
- [trace](datarouter-trace/README.md)
  - Collects and publishes tracing data defined by datarouter-instrumentation
- [webapp-instance](datarouter-webapp-instance/README.md)
  - Collects stats about running servers in a service

### Server data features
- [copy-table](datarouter-copy-table/README.md)
  - Copies databeans from one table to another, usually for migrating from one database to another
- [data-export](datarouter-data-export/README.md)
  - Exports databeans from a table to BlobStorage
- [nodewatch](datarouter-nodewatch/README.md)
  - Collects table statistics in background joblets and displays them
  - Stores every Nth key in each table enabling parallel processing later
- [snapshot](datarouter-snapshot/README.md)
  - File format for storing many objects in BlobStorage with random read access
- [snapshot-manager](datarouter-snapshot-manager/README.md)
  - UI for tracking many snapshots and groups of snapshots

## Data storage clients
#### Tables
- [mysql](datarouter-mysql/README.md)
  - Map, Sorted, Indexed, Blob, Tally storage
- [gcp-spanner](datarouter-spanner/README.md)
  - Map, Sorted, Indexed, Blob, Tally storage
- [hbase](datarouter-hbase/README.md)
  - Map, Sorted storage
- [gcp-bigtable-hbase](datarouter-gcp-bigtable-hbase/README.md)
  - Map, Sorted storage
#### Caches
- [memcached](datarouter-memcached/README.md)
  - Map, Blob, Tally storage
- [redis](datarouter-redis/README.md)
  - Map, Blob, Tally storage
#### Messages
- [aws-sqs](datarouter-aws-sqs/README.md)
  - Queue, GroupQueue, BlobQueue storage
- [gcp-pubsub](datarouter-gcp-pubsub/README.md)
  - Queue, GroupQueue, BlobQueue storage
#### Blobs
- [s3](datarouter-aws-s3/README.md)
  - Blob storage
- [filesystem](datarouter-filesystem/README.md)
  - Blob storage
#### Memory
- [memory](datarouter-memory/README.md)
  - Implement many storage interfaces in memory for high performance
  - A spec for how other clients should behave
  - Map, Sorted, Blob, Queue, GroupQueue, BlobQueue, Tally storage

## Wrappers around other libraries
- [checkstyle](datarouter-checkstyle/README.md)
  - Checkstyle configuration used by datarouter
- [email](datarouter-email/README.md)
- [gson](datarouter-gson/README.md)
  - Gson TypeAdapters used elsewhere in datarouter
- [inject](datarouter-inject/README.md)
  - Generic injector interface with default Guice implementation
- [logging](datarouter-logging/README.md)
  - Additions to Log4j2
- [mockito](datarouter-mockito/README.md)
- [opencensus](datarouter-opencensus/README.md)
- [testng](datarouter-testng/README.md)

## AWS utils
- [aws-alb](datarouter-aws-alb/README.md)
  - Application Load Balancer configuration and monitoring
- [aws-rds](datarouter-aws-rds/README.md)
  - Relational Database Service configuration and monitoring
- [aws-secrets-manager](datarouter-aws-secrets-manager/README.md)
  - Client implmenentation for writing to and reading from AWS Secrets Manager

## Parent modules
- [metadata-parent](datarouter-metadata-parent/README.md)
- [parent](datarouter-parent/README.md)
- [java17-parent](datarouter-java17-parent/README.md)
- [full-build](datarouter-full-build/README.md)

## Example webapp
- [example](datarouter-example/README.md)
  - Tiny web application with examples of Databeans, Handlers, Settings, Jobs, etc
