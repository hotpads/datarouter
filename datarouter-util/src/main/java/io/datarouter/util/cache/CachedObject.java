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
package io.datarouter.util.cache;

import java.time.Clock;
import java.time.Duration;

public record CachedObject<V>(
		V value,
		long timeOfExpirationMs){

	public CachedObject(V value, Clock clock, Duration ttl){
		this(value, Clock.offset(clock, ttl).millis());
	}

	public boolean isExpired(Clock currentTime){
		return currentTime.millis() > timeOfExpirationMs;
	}

}
