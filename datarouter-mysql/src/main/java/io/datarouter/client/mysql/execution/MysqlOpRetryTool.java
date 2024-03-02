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
package io.datarouter.client.mysql.execution;

import io.datarouter.storage.config.Config;
import io.datarouter.util.retry.RetryableTool;

public class MysqlOpRetryTool{

	public static final int NUM_ROLLBACK_ATTEMPTS = 5;
	public static final long ROLLBACK_BACKOFF_MS = 4;

	//this defaults to 1, so you must explicitly call config.setNumAttempts(x) to get retries on
	// non-MySQLTransactionRollbackExceptions
	public static final int DEFAULT_NUM_ATTEMPTS = 1;
	public static final long DEFAULT_BACKOFF_MS = 1;


	/* This executes the query config.getNumAttempts() times. Then for each attempt, it will retry rollbacks a fixed
	 * number of times (NUM_ROLLBACK_ATTEMPTS - 1). If config.getNumAttempts() is 2 and NUM_ROLLBACK_ATTEMPTS is 3, then
	 * we may start 6 txns */
	public static <T> T tryNTimes(SessionExecutorCallable<T> opCallable, Config config){
		var retryingCallable = new MysqlRollbackRetryingCallable<>(
				opCallable,
				NUM_ROLLBACK_ATTEMPTS,
				ROLLBACK_BACKOFF_MS);
		int numAttempts = config.findNumAttempts().orElse(DEFAULT_NUM_ATTEMPTS);
		boolean ignoreExceptions = config.findIgnoreException().orElse(false);
		return RetryableTool.tryNTimesWithBackoffUnchecked(
				retryingCallable,
				numAttempts,
				DEFAULT_BACKOFF_MS,
				!ignoreExceptions);
	}

}
