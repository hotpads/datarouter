# datarouter-joblet

A Joblet is a tiny job that is executed asynchronously across a pool of servers.  

The datarouter-joblet module helps you build a user-friendly queue processing system inside your application.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-joblet</artifactId>
	<version>0.0.102</version>
</dependency>
```

## Installation with Datarouter

Datarouter-joblet brings in `BaseJobletPlugin` and `DatarouterJobletWebappBuilder`. `BaseJobletPlugin` brings in 
everything from `BaseJobPlugin` and adds the ability to register joblets. `DatarouterJobletWebappBuilder` provides 
an easy way to bootstrap the application and install web, job or joblet plugins. 

## Concepts

#### Items per joblet

Each joblet usually contains multiple items for processing, though a single item is also fine.  We recommend putting 
enough items in a joblet that it executes for about 10 seconds.  Fewer items per joblet causes more overhead for the 
joblet system, while more items leads to less predictable behavior.

#### JobletPriority

Joblets can be enqueued with one of three priorities: `HIGH`, `DEFAULT`, or `LOW`.  Your code can decide the priority
for each joblet at runtime based on its content.  For example, you might have a subset of data that should process
at high priority, while you might requeue all your data periodically at low priority for consistency checking.

#### JobletStatus

Each `JobletRequest` is created with the `CREATED` status.  The `status` field is updated as it is processed.

- `CREATED`
  - Initial status meaning there should be a pending message in the queue (message is `visible` in SQS)
- `RUNNING`
  - Request has been dequeued, joblet is processing, and the queue considered the message checked out (`not visible` in
  SQS)
- `COMPLETE`
  - Currently used for testing, but may be included as a production option for retention of completed messages
- `INTERRUPTED`
  - Usually caused by server being shutdown
- `FAILED`
  - An exception occurred while processing.  The `numFailures` field should also be incremented
- `TIMED_OUT`
  - Joblets should stop themselves before overrunning the 10 minute limit, however if they do not, the joblet system
  may set their status to `TIMED_OUT` indicating another thread may try to run them.

#### JobletParams

Define a `JobletParams` class for each joblet type as a wrapper for its inputs.  This can contain any number of types
that get serialized and stored in a JobletData object.  The joblet system handles the serialization for you and will
pass the deserialized params to the joblets on whichever server it runs.

#### JobletCodec

`JobletCodec` serializes the JobletParams to a string.  We recommend extending `BaseGsonJobletCodec` to leverage Gson
serialization, but it's also possible to implement something custom for smaller storage size.

Override the `calculateNumItems` method to tell the joblet system how many items are in each joblet.  This helps later
for monitoring how many items are in the queue and the processing rate of items, as opposed to full joblets.

#### Restartable

When enqueueing each joblet, you specify whether it's `restartable` or not.  Ideally, the behavior of a joblet is
idempotent, meaning the joblet can run any number of times without negative consequences.  The joblet system will
retry failed joblets 2 times before leaving them with the `FAILED` status.

It currently does not have any strong locking mechanism to prevent multiple accidental runs, but it won't purposely
restart joblets with `restartable=false`.

#### Timeouts

After a joblet with `restartable=true` has been "checked out" by a server for 10 minutes without completing, another
processing thread will start to run the joblet.  Hence, it's important to try to beat this deadline by a large margin
during normal processing.

#### JobletType

A `JobletType` is the final definition used for registration:
  - `persistentString`
    - The name of the joblet, usually in PascalCase.
  - `shortQueueName`
    - A string embedded in the messaging system's queue name, which potentially has length restrictions.  The current 
    max length is 38 chars.
  - `codecSupplier`
    - A reference to the JobletCodec's constructor, for example `SleepingJoblet::new`.
  - `clazz`
    - The class that extends `BaseJoblet` and implements the `process()` method, for example `SleepingJoblet.class`.

#### JobletRequest

`JobletRequest` is a `Databean` holding the metadata about each queued joblet.  

The `PrimaryKey` fields are chosen to order joblets for dequeuing directly from the database if configured to do so.
The `JobletQueueMechanism` can scan through joblets of a given type/executionOrder and find the first available for 
processing.

  - `type`
    - the string defined in `JobletType`
  - `executionOrder`
    - the inverse of `JobletPriority` where `HIGH` priority has the lowest value
  - `created`
    - the epoch millisecond time of creation
  - `batchSequnce`
    - integer (usually random) to differentiate between joblets created in the same millisecond.

When using a backing messaging system like SQS, a message is queued with the contents of each `JobletRequest`, but the 
`JobletRequest` is also stored in the database as the source of truth.  When a joblet completes successfully, the
`JobletRequest` is deleted from the database, however if it hits the failure threshold it's left in the database
for further investigation.

#### JobletRequest duplication

Why the duplication between database and messaging system?  Messaging systems can be difficult to operate when things
aren't going as planned.

For example, sometimes a message/joblet will fail to process due to invalid params.  With joblets you can delete the 
record from the database, and the queue message will be automatically discarded.

Or maybe a bug in the code caused many joblets to fail.  Because they are still in the database, you can deploy a code
fix and use the UI to restart those joblets, which copies them back into the messaging system.

Further, because the JobletRequest databean has more metadata like the creation date and number of items, we can do
more sophisticated monitoring of processing backlogs and throughput.

#### JobletData

For each `JobletRequest` there is a corresponding `JobletData` record that holds the serialized `JobletParams`.  This
is a standalone table to support potentially large params.  The `JobletRequest` table is often scanned for monitoring
purposes, and offloading the params to a separate table keeps that scanning fast.  It also avoids passing the params
through the messaging system which may have smaller message size restrictions.

#### JobletPackage

A wrapper class for a `JobletRequest` and `JobletData` duo.  Worth noting because this is the atomic unit you create when
submitting a joblet to `JobletService::submitJobletPackages`.

#### JobletQueueMechanism

Selecting available JobletRequests from a queue with multiple threads is a performance-sensitive operation.  Traditional
messaging systems like SQS are designed to do this efficiently, so the `JobletQueueMechanism.SQS` is the preferred
option for production, configurable via cluster setting.

Through the `DatarouterJobletWebappBuilder` you can specify different selectors. For example, with `datrouter-joblet-mysql`
 there  is a `JobletQueueMechanism.JDBC_LOCK_FOR_UPDATE` that uses `datarouter-mysql` with transactions as the backing store.
This is an option for development or staging environments where you don't need high throughput and want
to avoid creating lots of extra SQS queues.  Note that as throughput from multiple threads ramps up it will suffer
from locking related timeouts.

## Example: SleepingJoblet

### JobletParams

Ususally defined as `static class` inside the parent joblet.

```java
public static class SleepingJobletParams{

	public final String id;
	public final long sleepTimeMs;
	public final int numFailures;

	public SleepingJobletParams(String id, long sleepTimeMs, int numFailures){
		this.id = id;
		this.sleepTimeMs = sleepTimeMs;
		this.numFailures = numFailures;
	}
	
}
```

### JobletCodec

Ususally defined as `static class` inside the parent joblet.

```java
public static class SleepingJobletCodec extends BaseGsonJobletCodec<SleepingJobletParams>{

	public SleepingJobletCodec(){
		super(SleepingJobletParams.class);
	}

	@Override
	public int calculateNumItems(SleepingJobletParams params){
		return 1;
	}

}
```

### Joblet

```java
public class SleepingJoblet extends BaseJoblet<SleepingJobletParams>{

	@Override
	public Long process(){
		long startMs = System.currentTimeMillis();
		long remainingMs = params.sleepTimeMs;
		int numPreviousFailures = getJobletRequest().getNumFailures();
		if(numPreviousFailures < params.numFailures){
			int thisFailureNum = numPreviousFailures + 1;
			String message = "SleepingJoblet intentional failure " + thisFailureNum + "/" + JobletRequest.MAX_FAILURES
					+ " on " + jobletRequest;
			throw new RuntimeException(message);
		}
		while(remainingMs > 0){
			assertShutdownNotRequested();
			ThreadTool.sleep(Math.min(remainingMs, MAX_SEGMENT_MS));
			long totalElapsedMs = System.currentTimeMillis() - startMs;
			remainingMs = params.sleepTimeMs - totalElapsedMs;
		}
		return params.sleepTimeMs;
	}
	
}
```

### JobletType

```java
public static final JobletType<SleepingJobletParams> JOBLET_TYPE = new JobletType<>(
		"SleepingJoblet",
		"Sleeping",
		SleepingJobletCodec::new,
		SleepingJoblet.class,
		true);
```

### Register the JobletType

In your `DatarouterWebappConfigBuilder` register the `JobletType`

```java
		.addJobletTypes(List.of(SleepingJoblet.JOBLET_TYPE))
```

## Monitoring

#### UI

The default `list` page of the joblet UI can be used to see current queue lengths, item counts, failure counts,
and oldest joblets.

Other options include viewing thread count configuration, creating `SleepingJoblet`s (for testing), linking to external
metrics, or restarting joblets.

#### External Metrics

The joblet web UI has hooks for linking to external monitoring systems that record datarouter metrics and exceptions.
By default a `NoOpJobletExternalLinkBuilder` is bound causing the links to be hidden, but you can customize the links by 
implementing the `JobletExternalLinkBuilder` interface and pass the implementation class to the 
`DatarouterJobletPlugin`.

## Settings & Scaling

### Primary Settings

The `datarouterJoblet` cluster settings page contains a global `runJoblets` setting as well as cusomizations like 
`queueMechanism`.

### Thread count settings

Cluster settings are automatically generated for each registered `JobletType` so you can control the thread counts
on each server and across the cluster.  Thread counts can be tuned to customize auto-scaling behavior.

#### Instance thread count

Instance thread count settings, under `datarouterJoblet.threadCount`, are used to set the max threads of each
`JobletType` per webapp instance.  A general rule of thumb is to set it high enough to use most of the CPU on the 
instance but not enough to cause excessive context switching.  If not configured high enough, the resulting CPU 
usage may not be high enough to trigger the cluster auto-scaler.

#### Cluster thread count

Cluster thread count settings, under `datarouterJoblet.clusterThreadCount`, are used to limit the total number of
simultaneous joblets of a given type running across the entire cluster.  With an auto-scaling cluster, you would
generally set this limit based on external resource limits.  For example, too many joblets might saturate one of
your databases.

#### Scaling behavior

The effective (runtime) instance maximum is determined by dividing the clusterThreadCount by the number of running
instances, so it can drop below the configured instance maximum as more instances are added.  Ideally, your cluster 
auto-scaler will add enough instances that each one is running fewer threads than its configured maximum, therefore 
using slightly less CPU than the auto-scaler's CPU threshold.

## Operations

#### Requeuing

In case you suspect any joblets are missing from the queue, the joblet UI has menus to requeue all joblets or joblets 
of a given type.  This is generally safe to do at any time because each message pulled from the messaging queue is 
checked against the database for validity.  If the joblet is missing from the database or already running, the message
will be silently discarded.

#### Restarting

If there are failed joblets that you'd like to retry, maybe after a code fix, you can restart them via the UI.  This
updates the database status to `created`, resets `numFailures` to zero, and requeues the joblets.  They are effectively
created from scratch.

#### Deleting

Each row in the joblet UI has a trash can icon allowing deletion of unwanted joblets.  This can be used if a joblet
is deleted from the code or if you are ok with deleting failed joblets.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
