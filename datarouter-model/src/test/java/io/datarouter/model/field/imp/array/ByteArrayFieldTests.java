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
package io.datarouter.model.field.imp.array;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ByteArrayFieldTests{

	@Test
	public void stringEncodedValue(){
		byte[] value = {
				0x1,
				0x5,
				-0x8,
				0x7f,
				0x25,
				0x6a,
				-0x80,
				-0x12
		};
		ByteArrayField byteArrayField = new ByteArrayField(new ByteArrayFieldKey("testField"),
				value);
		byte[] encodedDecodedValue = byteArrayField.parseStringEncodedValueButDoNotSet(byteArrayField
				.getStringEncodedValue());
		Assert.assertEquals(encodedDecodedValue, value);
	}

}
