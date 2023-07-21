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

import java.util.List;
import java.util.Map;

import io.datarouter.scanner.Scanner;

/**
 * Common interface to programmatically inject without knowing the implementation library (Guice, Spring...)
 */
public interface DatarouterInjector{

	<T> T getInstance(Class<T> clazz);
	<T> Map<String,T> getInstancesOfType(Class<T> type);
	void injectMembers(Object instance);

	default <T> List<? extends T> getInstances(List<Class<? extends T>> classes){
		return Scanner.of(classes)
				.map(this::getInstance)
				.list();
	}

	default <T> Scanner<T> scanValuesOfType(Class<T> type){
		return Scanner.of(getInstancesOfType(type).values());
	}

}
