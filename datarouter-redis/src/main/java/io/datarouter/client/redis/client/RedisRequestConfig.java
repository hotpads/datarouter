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
package io.datarouter.client.redis.client;

import java.time.Duration;

import io.datarouter.storage.config.Config;

public class RedisRequestConfig{

	private static final boolean DEFAULT_IGNORE_EXCEPTION_FOR_READ = true;
	private static final boolean DEFAULT_IGNORE_EXCEPTION_FOR_WRITE = false;
	private static final Duration DEFAULT_TIMEOUT_FOR_READ = Duration.ofSeconds(3);
	private static final Duration DEFAULT_TIMEOUT_FOR_WRITE = Duration.ofSeconds(3);

	public final String caller;
	public final Duration timeout;
	public final boolean ignoreException;

	private RedisRequestConfig(String caller, Duration timeout, boolean ignoreException){
		this.caller = caller;
		this.timeout = timeout;
		this.ignoreException = ignoreException;
	}

	public static RedisRequestConfig forRead(String caller, Config config){
		return new RedisRequestConfig(
				caller,
				config.findTimeout().orElse(DEFAULT_TIMEOUT_FOR_READ),
				config.findIgnoreException().orElse(DEFAULT_IGNORE_EXCEPTION_FOR_READ));
	}

	public static RedisRequestConfig forWrite(String caller, Config config){
		return new RedisRequestConfig(
				caller,
				config.findTimeout().orElse(DEFAULT_TIMEOUT_FOR_WRITE),
				config.findIgnoreException().orElse(DEFAULT_IGNORE_EXCEPTION_FOR_WRITE));
	}
}