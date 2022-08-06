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
package io.datarouter.auth.service;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;

@Singleton
public class CallerTypeByAccountNameCache{

	private final LoadingCache<String,String> cache;

	@Inject
	public CallerTypeByAccountNameCache(DatarouterAccountDao datarouterAccountDao){
		CacheLoader<String,String> cacheLoader = new CacheLoader<>(){
			@Override
			public String load(String key){
				return datarouterAccountDao.get(new DatarouterAccountKey(key))
						.getCallerType();
			}
		};
		cache = Caffeine.newBuilder()
				.refreshAfterWrite(Duration.ofSeconds(15))
				.maximumSize(100)
				.build(cacheLoader);
	}

	public String get(String accountName){
		return cache.get(accountName);
	}

}
