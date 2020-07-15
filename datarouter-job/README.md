# datarouter-job

datarouter-job makes it easy to schedule jobs across a cluster of servers.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-job</artifactId>
	<version>0.0.40</version>
</dependency>
```
## Installation with Datarouter

Datarouter-job brings in `BaseJobPlugin` and `DatarouterJobWebappBuilder`. `BaseJobPlugin` brings in everything 
from `BaseWebPlugin` and adds the ability to add TriggerGroups. `DatarouterJobWebappBuilder` provides an easy 
way to bootstrap the application and install web or job plugins. 

## Usage

### BaseJob

Create a job by extending BaseJob:

```java
package io.datarouter.job.readme;

import java.time.Instant;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.config.DatarouterProperties;

public class ExampleJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(ExampleJob.class);

	@Inject
	private DatarouterProperties datarouterProperties;

	@Override
	public void run(TaskTracker tracker){
		logger.warn("Running on {} at {}",
				datarouterProperties.getServerName(),
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

import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;

@Singleton
public class ExampleTriggerGroup extends BaseTriggerGroup{

	public ExampleTriggerGroup(){
		super("Example"); // category name
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
