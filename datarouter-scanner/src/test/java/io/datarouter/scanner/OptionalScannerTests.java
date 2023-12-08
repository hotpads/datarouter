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
import java.util.Optional;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionalScannerTests{

	@Test
	public void testOptionalOf(){
		List<Optional<Integer>> input = List.of(Optional.of(1), Optional.empty(), Optional.of(2));
		List<Integer> output = Scanner.of(input)
				.concat(OptionalScanner::of)
				.list();
		Assert.assertEquals(output, List.of(1, 2));
	}

	@Test
	public void testOfOptionalAmbiguous(){
		List<Integer> output = Scanner.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(2))
				.concat(OptionalScanner::of)
				.list();
		Assert.assertEquals(output, List.of(1, 2));
	}

	@Test
	public void testConcatOpt(){
		List<Integer> output = Scanner.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(2))
				.concatOpt(Function.identity())
				.list();
		Assert.assertEquals(output, List.of(1, 2));
	}

	@Test
	public void testNulls(){
		Scanner<Optional<Integer>> scanner = Scanner.of(
				Optional.empty(),
				null,
				Optional.of(2));
		Assert.assertThrows(
				NullPointerException.class,
				() -> scanner.concatOpt(Function.identity()).count());
	}


}
