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
package io.datarouter.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.tuple.Pair;

public class StreamTool{

	public static <T> Stream<T> stream(Iterable<T> iterable){
		return StreamSupport.stream(IterableTool.nullSafe(iterable).spliterator(), false);
	}

	public static <T> Stream<T> stream(Iterator<T> iterator){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
	}

	public static <A,T> List<T> map(Stream<A> stream, Function<A,T> mapper){
		return stream.map(mapper).collect(Collectors.toList());
	}

	public static <A,T> Set<T> mapToSet(Stream<A> stream, Function<A,T> mapper){
		return stream.map(mapper).collect(Collectors.toSet());
	}

	public static <T> Stream<T> nullItemSafeStream(Iterable<T> iterable){
		return stream(iterable).filter(Objects::nonNull);
	}

	public static <V> BinaryOperator<V> throwingMerger(){
		return (v1, v2) -> {
			throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
		};
	}

	/**
	 * For filtering by and mapping stream to stream of a subtype. Example:
	 * <pre>animals.stream()
	 * 		.flatMap(StreamTool.instancesOf(Cat.class))
	 * 		.forEach(Cat::meow);
	 * </pre>
	 */
	public static <E> Function<Object,Stream<E>> instancesOf(Class<E> clazz){
		return obj -> clazz.isInstance(obj) ? Stream.of(clazz.cast(obj)) : Stream.empty();
	}

	public static <A,B> Collector<Pair<A,B>,?,Map<A,B>> pairsToMap(){
		return Collectors.toMap(Pair::getLeft, Pair::getRight);
	}

	public static <A,B> Collector<Entry<A,B>,?,Map<A,B>> entriesToMap(){
		return Collectors.toMap(Entry::getKey, Entry::getValue);
	}

}
