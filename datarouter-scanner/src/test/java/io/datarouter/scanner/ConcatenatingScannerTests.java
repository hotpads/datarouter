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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcatenatingScannerTests{

	private static final Supplier<RuntimeException> EXCEPTION = () -> new RuntimeException(
			"This scanner should just process its first element");

	@Test
	public void testConcat(){
		Scanner<List<Integer>> batches = Scanner.of(
				List.of(),
				List.of(0, 1),
				List.of(2, 73),
				List.of(),
				List.of(3, 4));
		List<Integer> actual = batches
				.concat(Scanner::of)
				.list();
		List<Integer> expected = List.of(0, 1, 2, 73, 3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testConcatIter(){
		Map<String,List<Integer>> map = new TreeMap<>(Map.of(
				"a", List.of(),
				"b", List.of(0, 1),
				"c", List.of(2, 73),
				"d", List.of(),
				"e", List.of(3, 4)));

		List<Integer> actual = Scanner.of(map.keySet())
				.concatIter(map::get)
				.list();
		List<Integer> expected = List.of(0, 1, 2, 73, 3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testLazyness(){
		AtomicBoolean seenOne = new AtomicBoolean();
		Scanner.of(1, 2)
				.concat(foo -> Scanner.of(foo, foo)
						.each($ -> {
							if(seenOne.getAndSet(true)){
								throw EXCEPTION.get();
							}
						}))
				.findFirst();

		seenOne.set(false);
		Scanner.of(1, 2)
				.concat(foo -> Scanner.of(foo, foo)
						.each($ -> {
							if(seenOne.getAndSet(true)){
								throw EXCEPTION.get();
							}
						}))
				.iterator()
				.hasNext();
	}

	/**
	 * This test is to demonstrate that using the iterator (or spliterator) of a Stream has a different behavior than
	 * using the Stream API. The flatMap operation is not lazy anymore, it loads unnecessary items in a buffer. When
	 * wrapping a Stream with a Scanner, we use the iterator to operate on the underlying Stream. Therefore, it's not
	 * recommended to wrap a Stream that has a flatMap operation with a Scanner. It's better to wrap the Stream earlier
	 * and use concatenate that does not have this behavior, as demonstrated above.
	 */
	@Test
	public void testStreamLazyness(){
		AtomicBoolean seenOne = new AtomicBoolean();
		Stream.of(1, 2)
				.flatMap(foo -> Stream.of(foo, foo)
						.peek($ -> {
							if(seenOne.getAndSet(true)){
								throw EXCEPTION.get();
							}
						}))
				.findFirst();

		try{
			seenOne.set(false);
			Stream.of(1, 2)
					.flatMap(foo -> Stream.of(foo, foo)
							.peek($ -> {
								if(seenOne.getAndSet(true)){
									throw EXCEPTION.get();
								}
							}))
					.iterator()
					.hasNext();
		}catch(RuntimeException e){
			return;
		}
		Assert.fail();
	}

}
