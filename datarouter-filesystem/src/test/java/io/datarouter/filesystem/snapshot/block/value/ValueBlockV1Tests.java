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
package io.datarouter.filesystem.snapshot.block.value;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.scanner.Scanner;

public class ValueBlockV1Tests{

	@Test
	public void test1(){
		List<String> input = List.of("a", "bb", "ccc");
		var encoder = new ValueBlockV1Encoder();
		Scanner.of(input)
				.map(String::getBytes)
				.map(value -> new SnapshotEntry(EmptyArray.BYTE, EmptyArray.BYTE, new byte[][]{value}))
				.forEach(entry -> encoder.add(entry, 0));
		byte[] bytes = encoder.encode().concat();
		var block = new ValueBlockV1(bytes);
		List<String> output = block.valueCopies()
				.map(String::new)
				.list();
		Assert.assertEquals(output, input);
	}

}
