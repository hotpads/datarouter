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
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IncludeScanner<T> extends BaseScanner<T>{

	private final Scanner<T> input;
	private final Predicate<? super T> predicate;

	public IncludeScanner(Scanner<T> input, Predicate<? super T> predicate){
		this.input = input;
		this.predicate = predicate;
	}

	@Override
	public boolean advance(){
		while(input.advance()){
			if(predicate.test(input.getCurrent())){
				current = input.getCurrent();
				return true;
			}
		}
		return false;
	}

	public static class IncludeScannerTests{

		@Test
		public void simpleTest(){
			Scanner<Integer> input = Scanner.of(0, 1, 2, 3, 4, 5);
			Predicate<Integer> predicate = i -> i % 2 == 0;
			List<Integer> actual = input.include(predicate).list();
			List<Integer> expected = Arrays.asList(0, 2, 4);
			Assert.assertEquals(actual, expected);
		}

	}

}
