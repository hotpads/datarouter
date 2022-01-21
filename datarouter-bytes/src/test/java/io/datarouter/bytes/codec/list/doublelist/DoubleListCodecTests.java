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
package io.datarouter.bytes.codec.list.doublelist;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DoubleListCodecTests{

	private static final DoubleListCodec DOUBLE_LIST_CODEC = DoubleListCodec.INSTANCE;

	@Test
	public void testToFromByteArray(){
		double one = 2.39483;
		double two = -583.2039;
		double three = 5;
		double four = -.0000001;
		List<Double> doubles = Arrays.asList(one, two, null, three, four);

		byte[] doubleBytes = DOUBLE_LIST_CODEC.encode(doubles);
		List<Double> result = DOUBLE_LIST_CODEC.decode(doubleBytes, 0);
		Assert.assertEquals(result, doubles);
	}

}
