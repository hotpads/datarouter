package com.hotpads.util.core.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.util.core.DrIterableTool;

public class StreamTool{

	public static <T> Stream<T> stream(Iterable<T> iterable){
		return StreamSupport.stream(DrIterableTool.nullSafe(iterable).spliterator(), false);
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> action){
		stream(iterable).forEach(action);
	}

	public static <A,T> List<T> map(Iterable<A> iterable, Function<A,T> mapper){
		return stream(iterable).map(mapper).collect(Collectors.toList());
	}

	public static <T> Stream<T> nullItemSafeStream(Iterable<T> iterable){
		return stream(iterable).filter( Objects::nonNull );
	}

	public static <T> Stream<T> flatten(Stream<Stream<T>> streams){
		return streams.reduce(Stream.empty(), Stream::concat);
	}

	/************** Tests *******************/

	public static class StreamToolTests{
		@Test
		public void testStreamFromNull(){
			Stream<?> stream = stream(null);
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

	}

}
