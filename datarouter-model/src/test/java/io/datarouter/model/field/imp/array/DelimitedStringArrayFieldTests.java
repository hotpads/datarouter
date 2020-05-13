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
package io.datarouter.model.field.imp.array;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DelimitedStringArrayFieldTests{

	@Test
	public void testRoundTrip(){
		List<String> inputs = List.of("abc", "xyz", "def");
		String encoded = DelimitedStringArrayField.encode(inputs, ",");
		Assert.assertEquals(encoded, "abc,xyz,def");
		List<String> decoded = DelimitedStringArrayField.decode(encoded, ",");
		Assert.assertEquals(decoded, inputs);
	}

	@Test
	public void testRoundTripNull(){
		List<String> input = null;
		String encoded = DelimitedStringArrayField.encode(input, ",");
		Assert.assertEquals(encoded, null);
		List<String> decoded = DelimitedStringArrayField.decode(encoded, ",");
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testRoundTripEmpty(){
		List<String> inputs = new ArrayList<>();
		String encoded = DelimitedStringArrayField.encode(inputs, ",");
		Assert.assertEquals(encoded, "");
		List<String> decoded = DelimitedStringArrayField.decode(encoded, ",");
		Assert.assertEquals(decoded, inputs);
	}

}