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
package io.datarouter.bytes.codec.doublecodec;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RawDoubleCodecTests{

	private static final RawDoubleCodec CODEC = RawDoubleCodec.INSTANCE;

	@Test
	public void testBytes1(){
		double valueA = 12354234.456D;
		byte[] abytes = CODEC.encode(valueA);
		double aback = CODEC.decode(abytes, 0);
		Assert.assertEquals(aback, valueA);

		double valueB = -1234568.456D;
		byte[] bbytes = CODEC.encode(valueB);
		double bback = CODEC.decode(bbytes, 0);
		Assert.assertEquals(bback, valueB);

		Assert.assertTrue(Arrays.compareUnsigned(abytes, bbytes) < 0);//positives and negatives are reversed
	}

}
