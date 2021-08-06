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

import io.datarouter.scanner.Java9.Entry;

public class ConcatenatingScannerTests{

	private static final Supplier<RuntimeException> EXCEPTION = () -> new RuntimeException(
			"This scanner should just process its first element");

	@Test
	public void testConcat(){
		Scanner<List<Integer>> batches = Scanner.of(
				Java9.listOf(),
				Java9.listOf(0, 1),
				Java9.listOf(2, 73),
				Java9.listOf(),
				Java9.listOf(3, 4));
		List<Integer> actual = batches
				.concat(Scanner::of)
				.list();
		List<Integer> expected = Java9.listOf(0, 1, 2, 73, 3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testConcatIter(){
		Map<String,List<Integer>> map = new TreeMap<>(Java9.mapOf(
				new Entry<>("a", Java9.listOf()),
				new Entry<>("b", Java9.listOf(0, 1)),
				new Entry<>("c", Java9.listOf(2, 73)),
				new Entry<>("d", Java9.listOf()),
				new Entry<>("e", Java9.listOf(3, 4))));

		List<Integer> actual = Scanner.of(map.keySet())
				.concatIter(map::get)
				.list();
		List<Integer> expected = Java9.listOf(0, 1, 2, 73, 3, 4);
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
		try{
			Stream.of(1, 2)
					.flatMap(foo -> Stream.of(foo, foo)
							.peek($ -> {
								if(seenOne.getAndSet(true)){
									throw EXCEPTION.get();
								}
							}))
					.findFirst();
		}catch(RuntimeException e){
			if(Java9.IS_JAVA_9){
				throw e;
			}
		}

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
