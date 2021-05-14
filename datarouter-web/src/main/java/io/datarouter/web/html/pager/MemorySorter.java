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
package io.datarouter.web.html.pager;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;

/**
 * Sorting utility for situations where all data can be collected in memory for each invocation.  Sort names can be
 * used in the UI.
 */
public class MemorySorter<T>{

	public final LinkedHashMap<String,SortOption<T>> sortOptions = new LinkedHashMap<>();

	public <U extends Comparable<? super U>>
	MemorySorter<T> withAsc(
			String name,
			Function<? super T,? extends U> keyExtractor){
		return with(name, keyExtractor, false);
	}

	public <U extends Comparable<? super U>>
	MemorySorter<T> withDesc(
			String name,
			Function<? super T,? extends U> keyExtractor){
		return with(name, keyExtractor, true);
	}

	private <U extends Comparable<? super U>>
	MemorySorter<T> with(
			String name,
			Function<? super T,? extends U> keyExtractor,
			boolean reversed){
		Comparator<? super T> keyComparator = Comparator.comparing(
				keyExtractor,
				Comparator.nullsFirst(Comparator.naturalOrder()));
		Comparator<T> comparator = Comparator.nullsFirst(keyComparator);
		if(reversed){
			comparator = comparator.reversed();
		}
		sortOptions.put(name, new SortOption<>(name, comparator));
		return this;
	}

	public MemorySorter<T> withComparator(String name, Comparator<T> comparator){
		sortOptions.put(name, new SortOption<>(name, comparator));
		return this;
	}

	public Map<String,String> getDisplayByValue(){
		return sortOptions.values().stream()
				.collect(Collectors.toMap(
						option -> option.name,
						option -> option.name,
						(a, b) -> a,
						LinkedHashMap::new));
	}

	public Scanner<T> apply(Scanner<T> scanner, String sort, boolean reversed){
		if(sortOptions.isEmpty()){
			return scanner;
		}
		SortOption<T> sortOption = sortOptions.getOrDefault(sort, sortOptions.values().iterator().next());
		Comparator<T> comparator = reversed ? sortOption.comparator.reversed() : sortOption.comparator;
		return scanner.sort(comparator);
	}

	public static class SortOption<T>{

		public final String name;
		public final Comparator<T> comparator;

		public SortOption(String name, Comparator<T> comparator){
			this.name = name;
			this.comparator = comparator;
		}

	}

}
