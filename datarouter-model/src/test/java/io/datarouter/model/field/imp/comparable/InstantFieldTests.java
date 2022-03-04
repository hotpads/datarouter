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
package io.datarouter.model.field.imp.comparable;

import java.time.Instant;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InstantFieldTests{

	@Test
	public void testEncodeAndDecodeBytes(){
		Instant input = Instant.ofEpochSecond(3, 5);
		byte[] expectedBytes = {0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 5};
		byte[] actualBytes = InstantField.encodeToBytes(input);
		Assert.assertEquals(actualBytes, expectedBytes);
		Instant output = InstantField.decodeFromBytes(actualBytes, 0);
		Assert.assertEquals(output, input);
	}

}
