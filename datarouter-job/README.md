# datarouter-job

datarouter-job makes it easy to schedule jobs across a cluster of servers.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-job</artifactId>
	<version>0.0.121</version>
</dependency>
```
## Installation with Datarouter

You can add a collection of jobs by adding a `TriggerGroup` to a Plugin or `WebappConfigBuilder`. 
```java
addPluginEntry(BaseTriggerGroup.KEY, ExampleTriggerGroup.class);
```

## Usage

### BaseJob

Create a job by extending BaseJob:

```java
package io.datarouter.job.readme;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.config.properties.ServerName;
import jakarta.inject.Inject;

public class ExampleJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(ExampleJob.class);

	@Inject
	private ServerName serverName;

	@Override
	public void run(TaskTracker tracker){
		logger.warn("Running on {} at {}",
				serverName.get(),
				Instant.now());
	}

}
```

The tracker param can be checked frequently during execution, and if tracker.shouldStop() returns true, the job should 
exit gracefully. This most often happens when the server is shutting down.

### BaseTriggerGroup

Schedule a job by registering it in a `TriggerGroup`. TriggerGroups support Quartz style cron expressions:

```java
package io.datarouter.job.readme;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Singleton;

@Singleton
public class ExampleTriggerGroup extends BaseTriggerGroup{

	public ExampleTriggerGroup(){
		super("Example", ZoneIds.AMERICA_NEW_YORK); // category name
		registerLocked(
				"3 * * * * ?", // trigger on the 3rd second of every minute
				() -> true, //  run unconditionally, or alternatively pass a dynamic setting
				ExampleJob.class, // the job class
				true); // alert if the job can't finish before the next trigger
	}

}

```

The category name is used for grouping jobs in the web UI.

Jobs can be scheduled with either registerParallel or registerLocked.

Parallel jobs will run on all servers on which they're enabled. This can be useful for something like sending a 
heartbeat from every server to an external service.

Locked jobs will trigger on all servers running the webapp, but each server will race to acquire an external MySQL lock
before running the job. Simple delay logic is included to help distribute jobs evenly between servers.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
