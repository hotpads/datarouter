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

import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PeekingScanner<T> extends BaseScanner<T>{

	private final Scanner<T> input;
	private final Consumer<? super T> consumer;

	public PeekingScanner(Scanner<T> input, Consumer<? super T> consumer){
		this.input = input;
		this.consumer = consumer;
	}

	@Override
	public boolean advance(){
		if(input.advance()){
			current = input.getCurrent();
			consumer.accept(current);
			return true;
		}
		return false;
	}

	public static class PeekingScannerTests{

		@Test
		public void simpleTest(){
			Scanner<String> input = Scanner.of("a", "b", "c");
			LongAdder counter = new LongAdder();
			List<String> output = input.peek(s -> counter.increment()).list();
			Assert.assertEquals(counter.longValue(), output.size());
		}

	}

}
