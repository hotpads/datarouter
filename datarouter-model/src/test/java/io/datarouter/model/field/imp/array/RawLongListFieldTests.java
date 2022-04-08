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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.LongArray;
import io.datarouter.model.field.imp.list.RawLongListField;
import io.datarouter.model.field.imp.list.RawLongListFieldKey;

public class RawLongListFieldTests{

	private static final RawLongListFieldKey FIELD_KEY = new RawLongListFieldKey("");

	@Test
	public void testByteAware(){
		LongArray a1 = new LongArray();
		a1.add(Long.MAX_VALUE);
		a1.add(Integer.MAX_VALUE);
		a1.add(Short.MAX_VALUE);
		a1.add(Byte.MAX_VALUE);
		a1.add(5);
		a1.add(0);
		var field = new RawLongListField(FIELD_KEY, a1);
		byte[] bytesNoPrefix = field.getBytes();
		Assert.assertEquals(bytesNoPrefix.length, a1.size() * 8);
		List<Long> a2 = new RawLongListField(FIELD_KEY, null).fromBytesButDoNotSet(bytesNoPrefix, 0);
		Assert.assertEquals(a1, a2);

		List<Long> value2 = new LongArray();
		var field2 = new RawLongListField(FIELD_KEY, value2);
		byte[] toBytes2 = field2.getBytes();
		List<Long> fromBytes2 = field.fromBytesButDoNotSet(toBytes2, 0);
		Assert.assertEquals(value2, fromBytes2);
	}

}