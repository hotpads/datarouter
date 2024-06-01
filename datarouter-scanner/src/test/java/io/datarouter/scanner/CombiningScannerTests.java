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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CombiningScannerTests{

	private static class Result{
		List<String> letters = new ArrayList<>();
		List<String> animals = new ArrayList<>();
	}

	@Test
	public void simpleTest(){
		Scanner<String> letters = Scanner.of("a", "b", "c", "d", "f");
		Scanner<String> animals = Scanner.of("ant", "aphid", "bee", "coyote", "crab", "elk", "fox", "frog");

		List<Result> results = CombiningScanner.builder(Result::new, String::compareTo)
				.addInput(
						letters,
						Function.identity(),
						(result, letter) -> result.letters.add(letter))
				.addInput(
						animals,
						string -> string.substring(0, 1).toLowerCase(),
						(result, animal) -> result.animals.add(animal))
				.build()
				.list();

		Assert.assertEquals(results.size(), 6);
		Assert.assertEquals(toString(results.get(0)), "a=ant,aphid");
		Assert.assertEquals(toString(results.get(1)), "b=bee");
		Assert.assertEquals(toString(results.get(2)), "c=coyote,crab");
		Assert.assertEquals(toString(results.get(3)), "d=");
		Assert.assertEquals(toString(results.get(4)), "=elk");
		Assert.assertEquals(toString(results.get(5)), "f=fox,frog");
	}


	private static String toString(Result result){
		return String.format(
				"%s=%s",
				String.join(",", result.letters),
				String.join(",", result.animals));
	}

}
