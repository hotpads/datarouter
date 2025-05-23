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

import java.util.Optional;

public abstract class CaffeineLoadingCacheWrapper<K,V> implements Cache<K,V>{

	private final CaffeineLoadingCache<K,V> cache;

	public CaffeineLoadingCacheWrapper(CaffeineLoadingCache<K,V> cache){
		this.cache = cache;
	}

	@Override
	public String getName(){
		return cache.getName();
	}

	@Override
	public Optional<V> get(K key){
		return cache.get(key);
	}

	@Override
	public V getOrThrow(K key){
		return cache.getOrThrow(key);
	}

	public boolean load(K key){
		return cache.load(key);
	}

	@Override
	public boolean contains(K key){
		return cache.contains(key);
	}

	@Override
	public void invalidate(){
		cache.invalidate();
	}

	@Override
	public CacheStats getStats(){
		return cache.getStats();
	}

}
