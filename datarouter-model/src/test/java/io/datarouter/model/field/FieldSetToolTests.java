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
package io.datarouter.model.field;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.util.Bytes;

public class FieldSetToolTests{

	@Test
	public void testGetConcatenatedValueBytes(){
		List<Field<?>> fields = List.of(
				new IntegerField(new IntegerFieldKey("a"), 55),
				new StringField(new StringFieldKey("b"), "abc"),
				new StringField(new StringFieldKey("c"), "xyz"));
		int lengthWithout = 4 + 3 + 1 + 3;
		int lengthWith = lengthWithout + 1;
		Bytes withoutTrailingByte = new Bytes(FieldTool.getConcatenatedValueBytesUnterminated(fields));
		Bytes withTrailingByte = new Bytes(FieldTool.getConcatenatedValueBytes(fields));
		Assert.assertEquals(withoutTrailingByte.getLength(), lengthWithout);
		Assert.assertEquals(withTrailingByte.getLength(), lengthWith);
	}

}
