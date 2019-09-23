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
package io.datarouter.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StreamToolTests{

	@Test
	public void testStreamFromNull(){
		Stream<?> stream = StreamTool.stream((Iterable<?>)null);
		Assert.assertEquals(stream.count(), 0L);
	}

	@Test
	public void testNullItemSafeStreamWithNullCollection(){
		Assert.assertEquals(StreamTool.nullItemSafeStream(null).count(), 0L);
	}

	@Test
	public void testNullItemSafeStreamWithNullItem(){
		List<Integer> inputs = Arrays.asList(1, 2, null, 4);
		Assert.assertEquals(StreamTool.nullItemSafeStream(inputs).count(), 3L);
	}

	@Test
	public void testNullItemSafeStreamWithoutNullItem(){
		List<Integer> inputs = Arrays.asList(1, 2, 3, 4);
		Assert.assertEquals(StreamTool.nullItemSafeStream(inputs).count(), 4L);
	}

	@Test
	public void testInstancesOf(){
		List<CharSequence> charSequences = Arrays.asList(new StringBuilder("a"), "b", "c", new StringBuilder("d"));
		List<String> strings = charSequences.stream()
				.flatMap(StreamTool.instancesOf(String.class))
				.collect(Collectors.toList());
		List<String> expected = Arrays.asList("b", "c");
		Assert.assertEquals(strings, expected);
	}

}
