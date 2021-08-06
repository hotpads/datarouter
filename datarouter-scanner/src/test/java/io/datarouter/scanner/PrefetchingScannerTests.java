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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrefetchingScannerTests{

	@Test
	public void test(){
		List<Integer> integers = IntStream.range(0, 20)
				.mapToObj(Integer::valueOf)
				.collect(Collectors.toList());
		ExecutorService exec = Executors.newFixedThreadPool(3);
		Assert.assertEquals(Scanner.of(integers).prefetch(exec, 3).list(), integers);
		exec.shutdown();
	}

}
