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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class InstanceInventory{

	private final Map<InstanceInventoryKey<?>,List<InstanceItem<?>>> map = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked") // safety enforced by the add method
	public <T> List<InstanceItem<T>> get(InstanceInventoryKey<T> key){
		List<InstanceItem<?>> list = map.get(key);
		if(list == null){
			return List.of();
		}
		List<InstanceItem<T>> result = new ArrayList<>();
		for(InstanceItem<?> instanceItem : list){
			result.add((InstanceItem<T>) instanceItem);
		}
		return result;
	}

	public <T> void add(InstanceInventoryKey<T> key, String name, T item){
		map.computeIfAbsent(key, $ -> new ArrayList<>())
				.add(new InstanceItem<>(name, item));
	}

	public record InstanceItem<T>(
			String name,
			T item){
	}

}
