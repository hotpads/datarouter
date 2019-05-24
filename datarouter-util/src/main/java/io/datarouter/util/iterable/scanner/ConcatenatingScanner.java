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
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.iterable.scanner.iterable.IterableScanner;

public class ConcatenatingScanner<T> implements Scanner<T>{

	private final Scanner<Scanner<T>> inputScanners;
	private boolean finished;
	private Scanner<T> currentInputScanner;

	public ConcatenatingScanner(Scanner<Scanner<T>> inputScanners){
		this.inputScanners = inputScanners;
		this.finished = false;
	}

	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		if(currentInputScanner == null){//first invocation
			return advanceInputScanner();
		}
		return currentInputScanner.advance() || advanceInputScanner();
	}

	@Override
	public T getCurrent(){
		return currentInputScanner.getCurrent();
	}

	private boolean advanceInputScanner(){
		while(inputScanners.advance()){//skip empty inputScanners
			currentInputScanner = inputScanners.getCurrent();
			if(currentInputScanner.advance()){
				return true;
			}
		}
		finished = true;
		return false;
	}


	public static class ConcatenatingScannerTests{

		@Test
		public void test(){
			List<List<Integer>> batches = Arrays.asList(
					Collections.emptyList(),
					Arrays.asList(0, 1),
					Arrays.asList(2, 73),
					Collections.emptyList(),
					Collections.emptyList(),
					Arrays.asList(3, 4));
			List<Integer> actual = Scanner.of(batches).mapToScanner(IterableScanner::new).concatenate().list();
			List<Integer> expected = Arrays.asList(0, 1, 2, 73, 3, 4);
			Assert.assertEquals(actual, expected);
		}

	}

}
