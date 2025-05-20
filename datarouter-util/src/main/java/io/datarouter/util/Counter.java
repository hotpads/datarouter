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
package io.datarouter.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public class Counter<T>{

	private final Map<T,Long> counter = new HashMap<>();

	/**
	 * Increment the count for key
	 *
	 * @return the new count for key
	 */
	public long increment(T key){
		return increment(key, 1);
	}

	public long increment(T key, long amount){
		Long cnt = counter.get(key);
		if(cnt == null){
			cnt = 0L;
		}
		cnt += amount;
		counter.put(key, cnt);
		return cnt;
	}

	/**
	 * Decrement the count for key
	 *
	 * @param key key
	 * @return the new count for key
	 */
	public long decrement(T key){
		return increment(key, -1);
	}

	public int getAsInt(T key){
		Long cnt = counter.get(key);
		return cnt == null ? 0 : cnt.intValue();
	}

	public long get(T key){
		return counter.getOrDefault(key, 0L);
	}

	public Map<T,Long> getCounts(){
		return counter;
	}

	public Set<T> getKeys(){
		return counter.keySet();
	}

	public int getNumCounts(){
		return counter.size();
	}

	public void setCount(T key, long count){
		counter.put(key, count);
	}

	public long getTotal(){
		return counter.values().stream()
				.mapToLong(Long::valueOf)
				.sum();
	}

	public record CounterCount<T>(
			T key,
			long value){

		public static final Comparator<CounterCount<?>> BY_VALUE = Comparator.comparingLong(CounterCount::value);

		public String valueWithCommas(){
			return NumberFormatter.addCommas(value);
		}

	}

	public Scanner<CounterCount<T>> scanCounts(){
		return Scanner.of(counter.entrySet())
				.map(entry -> new CounterCount<>(entry.getKey(), entry.getValue()));
	}

	public List<CounterCount<T>> listCountsAsc(){
		return scanCounts()
				.sort(CounterCount.BY_VALUE)
				.list();
	}

	public List<CounterCount<T>> listCountsDesc(){
		return scanCounts()
				.sort(CounterCount.BY_VALUE.reversed())
				.list();
	}

	@Override
	public String toString(){
		return counter.entrySet().stream()
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.joining(", ", "{", "}"));
	}

}
