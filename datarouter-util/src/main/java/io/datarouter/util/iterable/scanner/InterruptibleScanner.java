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
package io.datarouter.util.iterable.scanner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InterruptibleScanner<T> extends BaseScanner<T>{

	private final Scanner<T> input;
	private final Supplier<Boolean> interruptor;

	public InterruptibleScanner(Scanner<T> input, Supplier<Boolean> interruptor){
		this.input = input;
		this.interruptor = interruptor;
	}

	@Override
	public boolean advance(){
		if(interruptor.get()){
			return false;
		}
		if(input.advance()){
			current = input.getCurrent();
			return true;
		}
		return false;
	}

	public static class InterruptibleScannerTests{

		@Test
		public void simpleTest(){
			AtomicLong counter = new AtomicLong();
			Scanner<Integer> input = Scanner.of(1, 2, 3);
			List<Integer> expected = Arrays.asList(1, 2);
			List<Integer> actual = input
					.peek(i -> counter.incrementAndGet())
					.interrupt(() -> counter.get() >= 2)
					.list();
			Assert.assertEquals(actual, expected);
		}
	}

}
