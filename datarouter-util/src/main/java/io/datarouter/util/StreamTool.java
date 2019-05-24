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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.testng.Assert;
import org.testng.annotations.Test;

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

	public static <T> Stream<T> flatten(Stream<Stream<T>> streams){
		return StreamSupport.stream(new FlatMappingSpliterator<>(streams.spliterator()), streams.isParallel());
	}

	public static <V> BinaryOperator<V> throwingMerger(){
		return (v1, v2) -> {
			throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
		};
	}

	public static <T> Stream<List<T>> batch(Stream<T> stream, int batchSize){
		return StreamSupport.stream(new BatchingSpliterator<>(stream.spliterator(), batchSize), false);
	}

	public static <T> Stream<List<T>> batch(Iterable<T> iterable, int batchSize){
		return batch(stream(iterable), batchSize);
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

	private static class BatchingSpliterator<T> extends AbstractSpliterator<List<T>>{

		private final int batchSize;
		private final Spliterator<T> original;

		private BatchingSpliterator(Spliterator<T> original, int batchSize){
			super(original.estimateSize() / batchSize, original.characteristics());
			this.original = original;
			this.batchSize = batchSize;
		}

		@Override
		public boolean tryAdvance(Consumer<? super List<T>> action){
			List<T> batch = new ArrayList<>(batchSize);
			while(batch.size() < batchSize && original.tryAdvance(batch::add)){
			}
			if(batch.isEmpty()){
				return false;
			}
			action.accept(batch);
			return true;
		}

	}

	private static class FlatMappingSpliterator<E> extends AbstractSpliterator<E>{

		private final Spliterator<Stream<E>> streams;
		private Spliterator<E> current;

		private FlatMappingSpliterator(Spliterator<Stream<E>> streams){
			super(streams.estimateSize(), streams.characteristics());
			this.streams = streams;
		}

		@Override
		public boolean tryAdvance(Consumer<? super E> action){
			do{
				if(current != null){
					if(current.tryAdvance(action)){
						return true;
					}
				}
			}while(streams.tryAdvance(stream -> current = stream.spliterator()));
			return false;
		}

	}

	/*--------------------------- Tests --------------------------------*/

	public static class StreamToolTests{

		@Test
		public void testStreamFromNull(){
			Stream<?> stream = stream((Iterable<?>)null);
			Assert.assertEquals(stream.count(), 0L);
		}

		@Test
		public void testNullItemSafeStreamWithNullCollection(){
			Assert.assertEquals(nullItemSafeStream(null).count(), 0L);
		}

		@Test
		public void testNullItemSafeStreamWithNullItem(){
			List<Integer> inputs = Arrays.asList(1, 2, null, 4);
			Assert.assertEquals(nullItemSafeStream(inputs).count(), 3L);
		}

		@Test
		public void testNullItemSafeStreamWithoutNullItem(){
			List<Integer> inputs = Arrays.asList(1, 2, 3, 4);
			Assert.assertEquals(nullItemSafeStream(inputs).count(), 4L);
		}

		@Test
		public void testBatch(){
			List<Integer> original = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
			List<List<Integer>> expected = Arrays.asList(
					Arrays.asList(3, 1),
					Arrays.asList(4, 1),
					Arrays.asList(5, 9),
					Arrays.asList(2, 6),
					Arrays.asList(5, 3),
					Arrays.asList(5));
			List<List<Integer>> batched = batch(original.stream(), 2).collect(Collectors.toList());
			Assert.assertEquals(batched, expected);
		}

		@Test
		public void testInstancesOf(){
			List<CharSequence> charSequences = Arrays.asList(new StringBuilder("a"), "b", "c", new StringBuilder("d"));
			List<String> strings = charSequences.stream()
					.flatMap(StreamTool.instancesOf(String.class))
					.collect(Collectors.toList());
			List<String> expected = Arrays.asList("b", "c");
			Assert.assertEquals(strings, expected);
		}

		@Test
		public void testFlatten(){
			Stream<Stream<Integer>> streams = Stream.of(Stream.of(1, 2), Stream.of(3, 4, 5), Stream.empty(),
					Stream.of(6));
			Assert.assertEquals(flatten(streams).collect(Collectors.toList()), Arrays.asList(1, 2, 3, 4, 5, 6));
		}
	}

	public static <A,B> Collector<Pair<A,B>,?,Map<A,B>> pairsToMap(){
		return Collectors.toMap(Pair::getLeft, Pair::getRight);
	}

	public static <A,B> Collector<Entry<A,B>,?,Map<A,B>> entriesToMap(){
		return Collectors.toMap(Entry::getKey, Entry::getValue);
	}

}
