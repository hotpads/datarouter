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
package io.datarouter.util.bytes;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;

public class ByteWriterTests{

	@Test
	public void test(){
		List<String> inputs = Scanner.iterate(0, i -> i + 1)
				.limit(20)
				.map(Long::toString)
				.list();
		String expected = Scanner.of(inputs)
				.collect(Collectors.joining());

		var writer = new ByteWriter(4);
		Scanner.of(inputs)
				.map(String::getBytes)
				.forEach(writer::bytes);
		String actual = new String(writer.concat());

		Assert.assertEquals(actual, expected);
	}

}
