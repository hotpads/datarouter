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
package io.datarouter.util.collector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collector.RelaxedMapCollector;

public class RelaxedMapCollectorTests{

	@Test
	public void testToMap(){
		List<String> list = Arrays.asList("a2", "bb3", "bb9", "c", "ddd4", "eeee5");
		Function<String,String> keyMapper = key -> key.startsWith("e") ? null : key.charAt(0) + "";
		Function<String,Integer> valueMapper = key -> {
			if(key.equals("c")){
				return null;
			}
			char lastChar = key.charAt(key.length() - 1);
			return Integer.parseInt(lastChar + "");
		};
		Map<String,Integer> map = list.stream().collect(RelaxedMapCollector.of(keyMapper, valueMapper));
		Iterator<Entry<String,Integer>> iterator = map.entrySet().iterator();
		Assert.assertEquals(nextIn(iterator), "a=2");
		Assert.assertEquals(nextIn(iterator), "b=9");
		Assert.assertEquals(nextIn(iterator), "c=null");
		Assert.assertEquals(nextIn(iterator), "d=4");
		Assert.assertEquals(nextIn(iterator), "null=5");
		Assert.assertFalse(iterator.hasNext());
	}

	private String nextIn(Iterator<Entry<String,Integer>> iterator){
		Entry<String,Integer> next = iterator.next();
		return next.getKey() + "=" + next.getValue();
	}

}
