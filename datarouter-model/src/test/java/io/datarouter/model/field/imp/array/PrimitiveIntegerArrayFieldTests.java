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

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveIntegerArrayFieldTests{

	@Test
	public void testByteSerialization(){
		int[] array = {1, 2, 100};
		PrimitiveIntegerArrayField field = new PrimitiveIntegerArrayField(new PrimitiveIntegerArrayFieldKey("test"),
				array);
		Assert.assertEquals(field.fromBytesButDoNotSet(field.getBytes(), 0), array);
	}

	@Test
	public void testStringEncodedValue(){
		PrimitiveIntegerArrayField field;

		int[] array = {1, -12, 15};
		String stringValue = "[1,-12,15]";
		field = new PrimitiveIntegerArrayField(new PrimitiveIntegerArrayFieldKey("test"), array);
		Assert.assertEquals(field.getStringEncodedValue(), stringValue);
		Assert.assertEquals(field.parseStringEncodedValueButDoNotSet(stringValue), array);

		int[] emptyArray = {};
		String emptyArrayStringValue = "[]";
		field = new PrimitiveIntegerArrayField(new PrimitiveIntegerArrayFieldKey("test"), emptyArray);
		Assert.assertEquals(field.getStringEncodedValue(), emptyArrayStringValue);
		Assert.assertEquals(field.parseStringEncodedValueButDoNotSet(emptyArrayStringValue), emptyArray);

		field = new PrimitiveIntegerArrayField(new PrimitiveIntegerArrayFieldKey("test"), null);
		Assert.assertNull(field.getStringEncodedValue());
		Assert.assertNull(field.parseStringEncodedValueButDoNotSet(null));
	}

}
