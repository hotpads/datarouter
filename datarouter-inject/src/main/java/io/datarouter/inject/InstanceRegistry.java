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
package io.datarouter.inject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.datarouter.util.StreamTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * For manually tracking instances in prototype scope or those created with 'new'
 */
@Singleton
public class InstanceRegistry{

	@Inject
	private DatarouterInjector datarouterInjector;

	private final Set<Object> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public <T> T register(T obj){
		set.add(obj);
		return obj;
	}

	private <T> Collection<T> getRegisteredInstancesOfType(Class<T> type){
		return set.stream()
				.flatMap(StreamTool.instancesOf(type))
				.collect(Collectors.toList());
	}

	public <T> Collection<T> getAllInstancesOfType(Class<T> type){
		Map<String,T> boundInstances = datarouterInjector.getInstancesOfType(type);
		Collection<T> manualInstances = getRegisteredInstancesOfType(type);
		manualInstances.addAll(boundInstances.values());
		return manualInstances;
	}

}
