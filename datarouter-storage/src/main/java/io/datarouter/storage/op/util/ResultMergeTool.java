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
package io.datarouter.storage.op.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.MapTool;

public class ResultMergeTool{

	public static Integer sum(Integer intA, Collection<Integer> ints){
		boolean foundValue = false;
		int sum = 0;
		if(intA != null){
			foundValue = true;
			sum += intA;
		}
		for(Integer i : CollectionTool.nullSafe(ints)){
			if(i != null){
				foundValue = true;
				sum += i;
			}
		}
		return foundValue ? sum : null;
	}

	public static Long sum(Long varA, Collection<Long> varC){
		boolean foundValue = false;
		long sum = 0;
		if(varA != null){
			foundValue = true;
			sum += varA;
		}
		for(Long i : CollectionTool.nullSafe(varC)){
			if(i != null){
				foundValue = true;
				sum += i;
			}
		}
		return foundValue ? sum : null;
	}

	//TODO rename?
	public static <T,C extends Collection<T>> T first(T objA, C otherObjects){
		if(objA != null){
			return objA;
		}
		for(T t : CollectionTool.nullSafe(otherObjects)){
			if(t != null){
				return t;
			}
		}
		return null;
	}

	public static <T,C extends Collection<T>> List<T> append(C objA, Collection<? extends C> otherObjects){
		int size = CollectionTool.sizeNullSafe(objA) + CollectionTool.getTotalSizeOfCollectionOfCollections(
				otherObjects);
		List<T> out = new ArrayList<>(size);
		out.addAll(CollectionTool.nullSafe(objA));
		for(C b : CollectionTool.nullSafe(otherObjects)){
			out.addAll(CollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T,C extends Collection<T>> Set<T> addAll(C src, Collection<? extends C> dest){
		Set<T> out = new HashSet<>();
		out.addAll(CollectionTool.nullSafe(src));
		for(C b : CollectionTool.nullSafe(dest)){
			out.addAll(CollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T,C extends Collection<T>> SortedSet<T> addAllSorted(C objA, Collection<? extends C> otherObjects){
		SortedSet<T> out = new TreeSet<>();
		out.addAll(CollectionTool.nullSafe(objA));
		for(C b : CollectionTool.nullSafe(otherObjects)){
			out.addAll(CollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T extends Comparable<? super T>,C extends Collection<T>>
	ArrayList<T> mergeIntoListAndSort(C objA, Collection<? extends C> bs){
		ArrayList<T> out = new ArrayList<>();
		out.addAll(CollectionTool.nullSafe(objA));
		for(C b : CollectionTool.nullSafe(bs)){
			out.addAll(CollectionTool.nullSafe(b));
		}
		Collections.sort(out);
		return out;
	}

	public static <K,V> Map<K,V> mergeMaps(Map<K,V> fromOnce, Collection<Map<K,V>> fromEach){
		Map<K,V> result = new HashMap<>();
		if(MapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,V> m : CollectionTool.nullSafe(fromEach)){
			result.putAll(m);
		}
		return result;
	}

}
