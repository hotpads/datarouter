/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.tasktracker.readme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import jakarta.inject.Inject;

public class ExampleTaskTrackerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(ExampleTaskTrackerHandler.class);

	@Inject
	private LongRunningTaskTrackerFactory trackerFactory;

	@Handler
	public Mav countFiles(String parentPath, Optional<Long> logEveryN) throws IOException{

		//create a TaskTracker
		TaskTracker tracker = trackerFactory.create(
				//determines the tracker name
				ExampleTaskTrackerHandler.class.getSimpleName(),
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
				.advanceUntil(_ -> tracker.shouldStop())
				.map(Object::toString)
				//update the item count and last item name, which also acts as a heartbeat
				.each(_ -> tracker.increment())
				.each(tracker::setLastItemProcessed)
				//log progress
				.sample(logEveryN.orElse(1L), true)
				.forEach(item -> logger.warn("{}={}", tracker.getCount(), item));

		//return a message to the user, obtaining the count from the tracker
		return new MessageMav("counted " + tracker.getCount());
	}

}
