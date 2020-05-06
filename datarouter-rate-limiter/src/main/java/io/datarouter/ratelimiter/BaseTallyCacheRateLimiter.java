/**
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
package io.datarouter.ratelimiter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.datarouter.ratelimiter.storage.BaseTallyDao;

public class BaseTallyCacheRateLimiter extends BaseCacheRateLimiter{

	private final BaseTallyDao tallyDao;

	public BaseTallyCacheRateLimiter(BaseTallyDao tallyDao, CacheRateLimiterConfig config){
		super(config);
		this.tallyDao = tallyDao;
	}

	// null returned indicated that the cache datastore failed the operation
	@Override
	protected Long increment(String key){
		return tallyDao.incrementAndGetCount(key, 1, getConfig().expiration, Duration.ofMillis(200));
	}

	@Override
	protected Map<String,Long> readCounts(List<String> keys){
		return tallyDao.getMultiTallyCount(keys, getConfig().expiration, Duration.ofMillis(200));
	}

}
