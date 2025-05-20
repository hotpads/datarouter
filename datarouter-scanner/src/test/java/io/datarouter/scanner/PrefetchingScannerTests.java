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
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrefetchingScannerTests{

	@Test
	public void testSimple(){
		List<Integer> integers = IntStream.range(0, 20)
				.boxed()
				.toList();
		ExecutorService exec = Executors.newFixedThreadPool(3);
		Assert.assertEquals(
				Scanner.of(integers)
						.prefetch(exec, 3)
						.list(),
				integers);
		exec.shutdown();
	}

	@Test
	public void testError(){
		List<Integer> integers = IntStream.range(0, 20)
				.boxed()
				.toList();
		ExecutorService exec = Executors.newFixedThreadPool(3);
		String message = "the message";
		try{
			Scanner.of(integers)
					.map(i -> {
						if(i == 5){
							throw new RuntimeException(message);
						}
						return i;
					})
					.prefetch(exec, 3)
					.list();
			Assert.fail("Shouldn't have succeeded");
		}catch(RuntimeException e){
			Assert.assertEquals(e.getMessage(), message);
		}finally{
			exec.shutdown();
		}
	}

}
