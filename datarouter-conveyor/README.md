# datarouter-conveyor

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-conveyor</artifactId>
	<version>0.0.83</version>
</dependency>
```

## Conveyors

A Conveyor is a set of threads that continuously process data in the background.  They are similar to jobs, but
 running continuously rather than at a scheduled trigger time. While any logic can be put in a conveyor, they are typically used to
drain data out of a memory buffer or messaging service like Amazon SQS.


### Buffers

Buffers are injectable, in-memory queues. A `MemoryBuffer` stores objects in memory with a specified size 
and is implemented with an `ArrayBlockingQueue`. A `DatabeanBuffer` is similar to a `MemoryBuffer`, but specifically 
stores databeans.  When calling `offer(databean)` on a buffer, the buffer will consider its configured maximum size to prevent memory 
exhaustion, discarding the databeans that don't fit.

## Use Cases

### Optimizing Latency

To optimize latency during a web request, you can create an in-memory `DatabeanBuffer` and send writes to the buffer with near-zero latency.
A conveyor would be configured to drain that memory buffer to persistent storage in the background.  The trade-off is that you might lose
the data in the memory buffer if the application crashes quickly, plus the loss of read-your-writes consistency in the database.

### Optimizing Availability

Messaging systems like SQS are designed to be simple and highly-available.  A web request could save databeans to SQS rather than a less reliable
database, and a conveyor could be configured to drain the SQS queue to the database in the background.  The trade-off is the extra complexity
and expense in the system, plus the loss of read-your-writes consistency in the database.


### Optimizing Throughput

Often times, requests generate a single databean or small number of databeans destined for the database.  If there are many such requests and
infrequent data loss is acceptable, then sending those databeans to a memory buffer can help reducing latency while improving throughput.  The
througput gains are due to the conveyor draining databeans from the memory buffer in larger batch sizes that can be more efficiently inserted
into the database.

### Combinations

An application that wants to flush a lot of data to the database with low latency, high availability, and high throughput could first save the
data to a memory buffer, drain it to a message queue with on conveyor, and then use a second conveyor to drain the message queue to a database
or other persistent storage.  The datarouter-conveyor module makes it easy to configure those workflows.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
