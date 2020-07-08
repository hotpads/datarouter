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
package io.datarouter.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PercentFieldCodecTests{

	private static final String ENCODED_INTERNAL_SEPARATOR = "%2F";//our separator must be encoded

	@Test
	public void testEncodeNulls(){
		List<String> inputs = new ArrayList<>();
		inputs.add(null);
		inputs.add(null);
		String encodedNulls = PercentFieldCodec.encode(inputs.stream());
		Assert.assertEquals(encodedNulls, "/");
	}

	@Test
	public void testDecodeEmptyStrings(){
		String encoded = "//";
		List<String> expected = List.of("", "", "");
		List<String> decoded = PercentFieldCodec.decode(encoded);
		Assert.assertEquals(decoded, expected);
	}

	@Test
	public void testIndividualCharacters(){
		// slash must be encoded
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of("/")), ENCODED_INTERNAL_SEPARATOR);
		// these 5 chars should not be encoded
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of("-")), "-");
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of("_")), "_");
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of(".")), ".");
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of("*")), "*");
		// nor should letters or digits
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of("Aa0")), "Aa0");
		// forward slash must be encoded because it's our separator
	}

	@Test
	public void testComplexStrings(){
		String input = "/ A-b_0/.c*m";
		String expected = ENCODED_INTERNAL_SEPARATOR + "+A-b_0" + ENCODED_INTERNAL_SEPARATOR
				+ ".c*m";
		Assert.assertEquals(PercentFieldCodec.encode(Stream.of(input)), expected);
	}

	@Test
	public void testMultipleStrings(){
		String[] inputs = new String[]{"*//a()&^$", "!~/.9"};
		String encoded = PercentFieldCodec.encode(Arrays.stream(inputs));
		List<String> decoded = PercentFieldCodec.decode(encoded);
		Assert.assertEquals(decoded, List.of(inputs));
	}

}