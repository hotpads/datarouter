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
package io.datarouter.bytes.codec.booleancodec;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class ComparableBooleanCodecTests{

	private static final ComparableBooleanCodec CODEC = ComparableBooleanCodec.INSTANCE;

	@Test
	public void testEncode(){
		Assert.assertEquals(CODEC.encode(false)[0], 0);
		Assert.assertEquals(CODEC.encode(true)[0], 1);
	}

	@Test
	public void testDecode(){
		Assert.assertEquals(CODEC.decode(new byte[]{0}, 0), false);
		Assert.assertEquals(CODEC.decode(new byte[]{1, 0}, 0), true);
		Assert.assertEquals(CODEC.decode(new byte[]{1, 0}, 1), false);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testInvalidAndExceptionType(){
		CODEC.decode(new byte[]{5});
	}

	@Test
	public void testCompare(){
		Assert.assertEquals(Boolean.compare(false, false), 0);
		Assert.assertEquals(Java9.compareUnsigned(CODEC.encode(false), CODEC.encode(false)), 0);

		Assert.assertEquals(Boolean.compare(true, true), 0);
		Assert.assertEquals(Java9.compareUnsigned(CODEC.encode(true), CODEC.encode(true)), 0);

		Assert.assertEquals(Boolean.compare(false, true), -1);
		Assert.assertEquals(Java9.compareUnsigned(CODEC.encode(false), CODEC.encode(true)), -1);

		Assert.assertEquals(Boolean.compare(true, false), 1);
		Assert.assertEquals(Java9.compareUnsigned(CODEC.encode(true), CODEC.encode(false)), 1);
	}

}
