package com.hotpads.guice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

public class GuiceTool{

	public static <T> List<T> getInstancesOfType(Injector injector, Class<T> type){
		List<T> instances = new ArrayList<>();
		for(Entry<Key<?>, Binding<?>> bindingEntry : injector.getAllBindings().entrySet()){
			Class<?> bindedType = bindingEntry.getKey().getTypeLiteral().getRawType();
			if(type.isAssignableFrom(bindedType)){
				T instance = type.cast(bindingEntry.getValue().getProvider().get());
				instances.add(instance);
			}
		}
		return instances;
	}
	
}
