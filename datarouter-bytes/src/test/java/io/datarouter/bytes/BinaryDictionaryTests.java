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
package io.datarouter.bytes;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class BinaryDictionaryTests{

	@Test
	public void testEmpty(){
		Assert.assertEquals(new BinaryDictionary().encode(), new byte[]{0});// VarInt size=0
	}

	@Test
	public void testDuplicates(){
		var dictionary = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"));
		Assert.assertThrows(IllegalArgumentException.class, () -> dictionary.put("k0", EmptyArray.BYTE));
	}

	@Test
	public void testValues(){
		var dictionary = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"))
				.put("k1", StringCodec.UTF_8.encode("v1"));
		byte[] actualBytes = dictionary.encode();
//		HexBlockTool.print(actualBytes);
		String hex = "02026b30027630026b31027631";
		Assert.assertEquals(actualBytes, HexBlockTool.fromHexBlock(hex));
		var decoded = BinaryDictionary.decode(actualBytes);
		Assert.assertEquals(decoded.find("k0").orElseThrow(), StringCodec.UTF_8.encode("v0"));
		Assert.assertEquals(decoded.find("k1").orElseThrow(), StringCodec.UTF_8.encode("v1"));
	}

	@Test
	public void testEncodedLength(){
		var dictionary = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"))
				.put("k1", StringCodec.UTF_8.encode("v1"));
		Assert.assertEquals(dictionary.encodedLength(), dictionary.encode().length);
	}

	@Test
	public void testEqualsAndHashCode(){
		var one = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"))
				.put("k1", StringCodec.UTF_8.encode("v1"));
		var two = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"))
				.put("k1", StringCodec.UTF_8.encode("v1"));
		Assert.assertEquals(one, two);
		Assert.assertEquals(one.hashCode(), two.hashCode());
		var three = new BinaryDictionary()
				.put("k0", StringCodec.UTF_8.encode("v0"))
				.put("k1", StringCodec.UTF_8.encode("different"));
		Assert.assertNotEquals(one, three);
		Assert.assertNotEquals(one.hashCode(), three.hashCode());
	}

}
