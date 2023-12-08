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

public abstract class LruTtlCacheWrapper<K,V>{

	private final LruTtlCache<K,V> cache;

	public LruTtlCacheWrapper(LruTtlCache<K,V> cache){
		this.cache = cache;
	}

	public V get(K key){
		return cache.get(key);
	}

	public boolean put(K key, V value){
		return cache.put(key, value);
	}

	public void invalidate(){
		cache.invalidate();
	}

	public boolean contains(K key){
		return cache.contains(key);
	}

}
