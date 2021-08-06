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
package io.datarouter.util.cached;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

//TODO rename to TimeCached? and move the cachedAtMs field from BaseCached to here.
public abstract class Cached<T> extends BaseCached<T>{

	protected volatile long cacheForMs;

	public Cached(long cacheFor, TimeUnit timeUnit){
		this.cacheForMs = timeUnit.toMillis(cacheFor);
	}

	public Cached(Duration ttl){
		this.cacheForMs = ttl.toMillis();
	}

	@Override
	protected boolean isExpired(){
		return System.currentTimeMillis() - cachedAtMs > cacheForMs;
	}

	public void expire(){
		synchronized(this){
			cachedAtMs = 0L;
			value = null;
		}
	}

}
