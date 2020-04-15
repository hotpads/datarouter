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
package io.datarouter.util.collection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MapToolTests{

	@Test
	public void getMapFromString(){
		String string = "key1: val1;key2: val2";
		Map<String,String> res = MapTool.getMapFromString(string, ";", ": ");
		Assert.assertEquals(res.size(), 2);
		Assert.assertEquals(res.get("key2"), "val2");
	}

	@Test
	public void testGetByKeyMapper(){
		List<String> strings = Arrays.asList("aaa", "b", "ca", "eeee", "ca");
		AtomicLong counterA = new AtomicLong(0);
		Function<String,String> valueMapper = str -> {
			if(str.contains("a")){
				return counterA.incrementAndGet() + "a";
			}
			if(str.contains("b")){
				return "b";
			}
			return str;
		};
		Map<Integer,String> containsByLength = MapTool.getBy(strings, String::length, valueMapper);
		Assert.assertEquals(containsByLength.keySet(), Arrays.asList(3, 1, 2, 4));
		Assert.assertEquals(containsByLength.values(), Arrays.asList("1a", "b", "3a", "eeee"));
	}

}
