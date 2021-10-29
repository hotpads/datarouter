# datarouter-task-tracker

## About

A TaskTracker implementation can be passed to a method that is expected to take some time to execute, whether 5 seconds or many minutes.  It is a hook for the method to
report back progress on the number of items processed or just a heartbeat.  In its simplest form, it provides a way for other threads to "requestStop" of the task
usually because the server is being stopped or a deadline has been reached, but can also be utilized for more detailed item counting, last item processed tracking, or 
status monitoring.

Tracked tasks should check the "shouldStop" method in an effort to gracefully stop processing, otherwise the framework may interrupt them.

The module provides a LongRunningTaskTracker with persistence of the task's progress that can be viewed in a web UI, both for real-time monitoring and for later debugging
if the tasks are not completing.  The datarouter-job framework automatically creates LongRunningTaskTrackers for each execution of a job.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-task-tracker</artifactId>
	<version>0.0.93</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterTaskTrackerPluginBuilder(...)
		...
		.build()
```

## Usage

### Automatic usage in job processing

When creating a scheduled job by extending BaseJob, the datarouter-job framework provides a TaskTracker to every execution of the job.  Jobs should check the
shouldStop method every 10ms to 2s in an effort to cleanly stop.  Frequent checks are kept performant by LongRunningTaskTracker's internal caching.

### Ad-hoc usage

A TaskTraker can be created manually when a task is anticipated to take a long time.  Here is a Handler that counts potentially many files in a directory:

```java
package io.datarouter.tasktracker.readme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.user.session.currentSessionInfo;

public class ExampleTaskTrackerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(ExampleTaskTrackerHandler.class);

	@Inject
	private LongRunningTaskTrackerFactory trackerFactory;

	@Handler
	public Mav countFiles(String parentPath, OptionalLong logEveryN) throws IOException{

		//create a TaskTracker
		TaskTracker tracker = trackerFactory.create(
				//determines the tracker name
				ExampleTaskTrackerHandler.class,
				//triggered by web request
				LongRunningTaskType.REQUEST,
				//deadline
				Instant.now().plus(Duration.ofMinutes(1)),
				//gracefully stop when the deadline is reached
				true,
				//record which user triggered the request, viewable in the UI
				getSessionInfo().getNonEmptyUsernameOrElse("anonymous"));

		//update and check the TaskTracker during a potentially long task
		Scanner.of(Files.walk(Paths.get(parentPath)))
				//check the deadline and short-circuit the scanner if it has been reached
				.advanceUntil($ -> tracker.shouldStop())
				.map(Object::toString)
				//update the item count and last item name, which also acts as a heartbeat
				.each($ -> tracker.increment())
				.each(tracker::setLastItemProcessed)
				//log progress
				.sample(logEveryN.orElse(1L), true)
				.forEach(item -> logger.warn("{}={}", tracker.getCount(), item));

		//return a message to the user, obtaining the count from the tracker
		return new MessageMav("counted " + tracker.getCount());
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
