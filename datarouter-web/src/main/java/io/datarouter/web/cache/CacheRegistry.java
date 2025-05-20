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
package io.datarouter.web.cache;

import java.util.Optional;

import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.cache.Cache;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CacheRegistry{

	@Inject
	private PluginInjector pluginInjector;

	public Scanner<Cache<?,?>> scan(){
		return pluginInjector.scanInstances(Cache.KEY);
	}

	public Optional<Cache<?, ?>> find(String cacheName){
		return scan().include(cache -> cache.getName().equals(cacheName)).findFirst();
	}
}
