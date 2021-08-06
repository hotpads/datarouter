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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.datarouter.util.number.NumberFormatter;

public class Count{

	private final String name;
	private final AtomicLong value;

	public Count(){
		this(null);
	}

	public Count(String name){
		this.name = name;
		this.value = new AtomicLong();
	}

	/*---------------- increment --------------*/

	//enable calling by method reference in streams
	public void increment(@SuppressWarnings("unused") Object ignored){
		value.incrementAndGet();
	}

	public void increment(){
		value.incrementAndGet();
	}

	public void incrementBy(long by){
		value.addAndGet(by);
	}

	public void incrementBySize(Collection<?> collection){
		value.addAndGet(collection.size());
	}

	public void incrementByLength(Object[] array){
		value.addAndGet(array.length);
	}

	/*---------------- decrement --------------*/

	public void decrement(@SuppressWarnings("unused") Object ignored){
		value.decrementAndGet();
	}

	public void decrement(){
		value.decrementAndGet();
	}

	public void decrementBy(long by){
		value.addAndGet(-by);
	}

	public void decrementBySize(Collection<?> collection){
		value.addAndGet(-collection.size());
	}

	public void decrementByLength(Object[] array){
		value.addAndGet(-array.length);
	}

	/*---------------- value --------------*/

	public long value(){
		return value.get();
	}

	public int intValue(){
		return value.intValue();
	}

	@Override
	public String toString(){
		return name + "=" + NumberFormatter.addCommas(value.get());
	}

	public static String toString(Collection<Count> counts){
		return counts.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", "));
	}

	public static class Counts{

		private final List<Count> counts = new ArrayList<>();

		public Count add(String name){
			Count count = new Count(name);
			counts.add(count);
			return count;
		}

		@Override
		public String toString(){
			return Count.toString(counts);
		}

	}

}
