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
package io.datarouter.joblet.queue;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import io.datarouter.util.Require;

@Singleton
public class JobletSelectorRegistry{

	private final Map<String,Class<? extends JobletRequestSelector>> selectorClassByName = new HashMap<>();

	public void register(String name, Class<? extends JobletRequestSelector> selectorClass){
		Require.notContains(selectorClassByName.keySet(), name, name + " was already registered");
		selectorClassByName.put(name, selectorClass);
	}

	public Class<? extends JobletRequestSelector> getSelectorClass(String name){
		return selectorClassByName.get(name);
	}

}
