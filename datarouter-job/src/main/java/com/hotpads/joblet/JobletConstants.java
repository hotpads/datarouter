package com.hotpads.joblet;

import java.time.Duration;

public class JobletConstants{

	public static final String QUEUE_PREFIX = "Joblet-";

	//  "my-namespace-" + "Joblet-" + shortQueueName + "-0100"
	public static final int
			MAX_LENGTH_NAMESPACE = 30,
			MAX_LENGTH_SHORT_QUEUE_NAME = 38;

	public static final long RUNNING_JOBLET_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();

}
