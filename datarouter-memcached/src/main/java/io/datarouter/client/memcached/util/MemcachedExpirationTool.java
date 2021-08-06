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
package io.datarouter.client.memcached.util;

import java.time.Duration;

import io.datarouter.storage.config.Config;

/*
 * The exp value is passed along to memcached exactly as given, and will be processed per the memcached protocol
 * specification:
 *
 * The actual value sent may either be Unix time (number of seconds since January 1, 1970, as a 32-bit value), or a
 * number of seconds starting from current time. In the latter case, this number of seconds may not exceed
 * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client is larger than that, the server will
 * consider it to be real Unix time value rather than an offset from current time.
 */
public class MemcachedExpirationTool{

	public static final int MAX = Integer.MAX_VALUE;

	public static int getExpirationSeconds(Config config){
		return config.findTtl()
				.map(Duration::toSeconds)
				.map(seconds -> Math.min(seconds, MAX))
				.map(Long::intValue)
				.orElse(0);
	}

}
