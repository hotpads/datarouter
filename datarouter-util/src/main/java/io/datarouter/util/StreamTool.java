package io.datarouter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.iterable.IterableTool;

public class StreamTool{

	public static <T> Stream<T> stream(Iterable<T> iterable){
		return StreamSupport.stream(IterableTool.nullSafe(iterable).spliterator(), false);
	}

	public static <T> Stream<T> stream(Iterator<T> iterator){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> action){
		stream(iterable).forEach(action);
	}

	public static <A, T> List<T> map(Iterable<A> iterable, Function<A,T> mapper){
		return map(stream(iterable), mapper);
	}

	public static <A, T> List<T> map(Stream<A> stream, Function<A,T> mapper){
		return stream.map(mapper).collect(Collectors.toList());
	}

	public static <T> Stream<T> nullItemSafeStream(Iterable<T> iterable){
		return stream(iterable).filter(Objects::nonNull);
	}

	public static <T> Stream<T> flatten(Stream<Stream<T>> streams){
		return streams.reduce(Stream.empty(), Stream::concat);
	}

	public static <V> BinaryOperator<V> throwingMerger(){
		return (v1, v2) -> {
			throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
		};
	}

	public static <T> Stream<List<T>> batch(Stream<T> stream, int batchSize){
		return StreamSupport.stream(new BatchingSpliterator<>(stream.spliterator(), batchSize), false);
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
			while(batch.size() < batchSize && original.tryAdvance(batch::add));
			if(batch.isEmpty()){
				return false;
			}
			action.accept(batch);
			return true;
		}

	}

	/************** Tests *******************/

	public static class StreamToolTests{
		@Test
		public void testStreamFromNull(){
			Stream<?> stream = stream((Iterable<?>)null);
			Assert.assertEquals(stream.count(), 0L);
		}

		@Test
		public void testForEach(){
			List<Integer> inputs = Arrays.asList(3, 7);
			AtomicLong total = new AtomicLong();
			forEach(inputs, input -> total.addAndGet(input));
			Assert.assertEquals(total.get(), 10);
		}

		@Test
		public void testMap(){
			List<String> names = Arrays.asList("Al", "Bob");
			List<String> greetings = map(names, name -> "Hello " + name);
			Assert.assertEquals(greetings.getClass(), ArrayList.class);//prefer ArrayList to LinkedList
			Assert.assertEquals(greetings.size(), 2);
			Assert.assertEquals(greetings.get(0), "Hello Al");
			Assert.assertEquals(greetings.get(1), "Hello Bob");
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
	}

}
