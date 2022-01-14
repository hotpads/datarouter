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
package io.datarouter.storage.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullsTool{
	private static final Logger logger = LoggerFactory.getLogger(NullsTool.class);

	private static final Set<String> locations = ConcurrentHashMap.newKeySet();
	private static final Set<String> stacks = ConcurrentHashMap.newKeySet();

	public static <T> T log(T item, String location){
		if(item == null){
			logIfNew(location);
		}
		return item;
	}

	public static <T> T log(T item, Object caller, String location){
		if(item == null){
			logIfNew(caller.getClass().getSimpleName() + "." + location);
		}
		return item;
	}

	public static <T> T log(T item, Class<?> callerClass, String location){
		if(item == null){
			logIfNew(callerClass.getSimpleName() + "." + location);
		}
		return item;
	}

	public static <T> T log(T item, Object caller){
		if(item == null){
			logIfNew(caller.getClass().getSimpleName());
		}
		return item;
	}

	public static <T> T log(T item, Class<?> callerClass){
		if(item == null){
			logIfNew(callerClass.getSimpleName());
		}
		return item;
	}

	public static <T> T logStackIfNull(T item){
		if(item == null){
			logIfNewStack();
		}
		return item;
	}

	private static void logIfNew(String location){
		if(locations.add(location)){
			logger.warn("added {} to {}", location, locations);
		}
	}

	private static void logIfNewStack(){
		String relevantStackTrace = StackWalker.getInstance().walk(stackStream ->
				stackStream
						.filter(stack -> !stack.getClassName().equals(NullsTool.class.getName()))
						.limit(10)
						.filter(stack -> stack.getClassName().contains("com.hotpads."))
						.map(stack -> stack.getClassName() + ":" + stack.getMethodName() + ":" + stack.getLineNumber())
						.collect(Collectors.joining("\n\t")));
		if(stacks.add(relevantStackTrace)){
			logger.warn("null for stack:\n\t{}", relevantStackTrace);
		}
	}

}
