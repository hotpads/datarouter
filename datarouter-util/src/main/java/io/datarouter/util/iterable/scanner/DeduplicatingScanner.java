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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DeduplicatingScanner<T> extends BaseScanner<T>{

	private final Scanner<T> input;

	public DeduplicatingScanner(Scanner<T> input){
		this.input = input;
	}

	@Override
	public boolean advance(){
		while(input.advance()){
			if(!input.getCurrent().equals(current)){
				current = input.getCurrent();
				return true;
			}
		}
		return false;
	}


	public static class DeduplicatingScannerTests{

		@Test
		public void simpleTest(){
			List<Integer> duplicates = Arrays.asList(0, 0, 1, 2, 3, 3, 4, 5, 9, 20, 20);
			List<Integer> expected = new ArrayList<>(new TreeSet<>(duplicates));
			List<Integer> actual = Scanner.of(duplicates).deduplicate().list();
			Assert.assertEquals(actual, expected);
		}

	}

}
