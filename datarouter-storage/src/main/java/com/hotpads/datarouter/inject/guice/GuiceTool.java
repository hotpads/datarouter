package com.hotpads.datarouter.inject.guice;

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
