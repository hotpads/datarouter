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
package io.datarouter.inject.guice;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;

public class GuiceTool{

	public static <T> Map<String,T> getInstancesOfType(Injector injector, Class<T> type){
		Map<String,T> instances = new HashMap<>();
		for(Entry<Key<?>, Binding<?>> bindingEntry : injector.getAllBindings().entrySet()){
			Class<?> bindedType = bindingEntry.getKey().getTypeLiteral().getRawType();
			if(type.isAssignableFrom(bindedType)){
				T instance = type.cast(bindingEntry.getValue().getProvider().get());
				instances.put(makeBindingName(bindingEntry.getValue()), instance);
			}
		}
		return instances;
	}

	private static String makeBindingName(Binding<?> binding){
		if(binding.getKey().getAnnotationType() == null){
			return Introspector.decapitalize(binding.getProvider().get().getClass().getSimpleName());
		}
		if(binding.getKey().getAnnotationType().equals(Named.class)){
			return ((Named)binding.getKey().getAnnotation()).value();
		}
		return Introspector.decapitalize(binding.getKey().getAnnotationType().getSimpleName());
	}

}
