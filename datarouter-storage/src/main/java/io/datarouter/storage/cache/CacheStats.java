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
package io.datarouter.storage.cache;

public record CacheStats(
		long hitCount,
		long missCount,
		long loadSuccessCount,
		long loadFailureCount,
		long totalLoadTime,
		long evictionCount,
		long evictionWeight){

	public static CacheStats of(long hitCount, long missCount, long loadSuccessCount, long loadFailureCount,
			long totalLoadTime, long evictionCount, long evictionWeight){
		return new CacheStats(
				hitCount,
				missCount,
				loadSuccessCount,
				loadFailureCount,
				totalLoadTime,
				evictionCount,
				evictionWeight);
	}

	public static CacheStats fromCaffeineCacheStats(com.github.benmanes.caffeine.cache.stats.CacheStats stats){
		return new CacheStats(
				stats.hitCount(),
				stats.missCount(),
				stats.loadSuccessCount(),
				stats.loadFailureCount(),
				stats.totalLoadTime(),
				stats.evictionCount(),
				stats.evictionWeight());
	}

}
