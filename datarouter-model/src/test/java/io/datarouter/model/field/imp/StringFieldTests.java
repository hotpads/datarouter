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
package io.datarouter.model.field.imp;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;

public class StringFieldTests{

	private static final StringFieldKey FIELD_KEY = new StringFieldKey("myName");
	private static final StringField FIELD = new StringField(FIELD_KEY, null);

	@Test
	public void testEmptyString(){
		byte[] bytes = EmptyArray.BYTE;
		String value = FIELD.fromKeyBytesWithSeparatorButDoNotSet(bytes, 0);
		Assert.assertEquals(value, "");
	}

	@Test
	public void testEmptyTrailingString(){
		byte[] bytes = {0, 0};
		String value = FIELD.fromKeyBytesWithSeparatorButDoNotSet(bytes, bytes.length);
		Assert.assertEquals(value, "");
	}

	@Test
	public void testNonEmptyTrailingString(){
		int offset = 2;
		byte[] bytes = {0, 0, 'o', 'k'};
		String value = FIELD.fromKeyBytesWithSeparatorButDoNotSet(bytes, offset);
		Assert.assertEquals(value, "ok");
	}

}
