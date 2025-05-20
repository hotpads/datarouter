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
package io.datarouter.scanner;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For tracking down modifications to Lists.
 * Logs each unique stack trace where the List is modified.
 * Meant to be temporary then replaced with an unmodifiable List.
 * Simple implementation restricted to RandomAccess Lists.
 */
public class WarnOnModifyList<E> extends AbstractList<E>
implements RandomAccess{
	private static final Logger logger = LoggerFactory.getLogger(WarnOnModifyList.class);

	private static final Set<String> stacks = ConcurrentHashMap.newKeySet();

	private final List<E> backingList;

	/*---------- construct ----------*/

	public WarnOnModifyList(List<E> backingList){
		Objects.requireNonNull(backingList);
		if(backingList instanceof RandomAccess){
			this.backingList = backingList;
		}else{
			throw new IllegalArgumentException("Input List must implement RandomAccess");
		}
	}

	/*------------ Collector ---------------*/

	public static final <T> Collector<T,?,List<T>> collector(){
		return Collectors.collectingAndThen(
				Collectors.toCollection(ArrayList::new),
				WarnOnModifyList::new);
	}

	/*----------- AbstractList reads ------------*/

	@Override
	public E get(int index){
		return backingList.get(index);
	}

	@Override
	public int size(){
		return backingList.size();
	}

	/*----------- AbstractList writes ------------*/

	@Override
	public boolean add(E value){
		logIfNewStack();
		return backingList.add(value);
	}

	@Override
	public void add(int index, E element){
		logIfNewStack();
		backingList.add(index, element);
	}

	@Override
	public E remove(int index){
		logIfNewStack();
		return backingList.remove(index);
	}

	@Override
	public E set(int index, E element){
		logIfNewStack();
		return backingList.set(index, element);
	}

	/*----------- private ------------*/

	private static void logIfNewStack(){
		String relevantStackTrace = StackWalker.getInstance().walk(stackStream -> stackStream
				.skip(1)// skip the logIfNewStack method
				.map(stack -> stack.getClassName() + ":" + stack.getMethodName() + ":" + stack.getLineNumber())
				.collect(Collectors.joining("\n\t")));
		if(stacks.add(relevantStackTrace)){
			String message = String.format("Unexpected List modification:\n\t%s", relevantStackTrace);
			logger.warn(message);
		}
	}

}
