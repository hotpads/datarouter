# datarouter-task-tracker

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-task-tracker</artifactId>
	<version>0.0.12</version>
</dependency>
```
## Usage

### Automatic usage in job processing

When creating a scheduled job by extending BaseJob, the datarouter-job framework provides a TaskTracker to every execution of the job.

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
import io.datarouter.web.user.session.CurrentUserSessionInfo;

public class ExampleTaskTrackerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(ExampleTaskTrackerHandler.class);

	@Inject
	private LongRunningTaskTrackerFactory trackerFactory;
	@Inject
	private CurrentUserSessionInfo currentUserSessionInfo;

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
				currentUserSessionInfo.getNonEmptyUsernameOrElse(request, "anonymous"));

		//update and check the TaskTracker during a potentially long task
		Scanner.of(Files.walk(Paths.get(parentPath)))
				//check the deadline and short-circuit the scanner if it has been reached
				.advanceUntil(item -> tracker.shouldStop())
				.map(Object::toString)
				//update the item count and last item name, which also acts as a heartbeat
				.peek($ -> tracker.increment())
				.peek(tracker::setLastItemProcessed)
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
