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
package io.datarouter.model.field;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.DoubleFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.util.bytes.ByteRange;
import io.datarouter.util.tuple.Pair;

public class FieldSetToolTests{

	@Test
	public void testGetConcatenatedValueBytes(){
		int someInt = 55;
		String someStringA = "abc";
		String someStringB = "xyz";
		List<Field<?>> fields = List.of(
				new UInt31Field(new UInt31FieldKey("someInt"), someInt),
				new StringField(new StringFieldKey("someStringA"), someStringA),
				new StringField(new StringFieldKey("someStringB"), someStringB));
		ByteRange withTrailingByte = new ByteRange(FieldTool.getConcatenatedValueBytes(fields, false, true, true));
		ByteRange withoutTrailingByte = new ByteRange(FieldTool.getConcatenatedValueBytes(fields, false, true,
				false));
		int lengthWithout = 4 + 3 + 1 + 3;
		int lengthWith = lengthWithout + 1;
		Assert.assertEquals(withTrailingByte.getLength(), lengthWith);
		Assert.assertEquals(withoutTrailingByte.getLength(), lengthWithout);
	}

	@Test
	public void testGenerateFieldMap(){
		int testInt = 127;
		String someStr0 = "first", someStr1 = "second";

		List<Field<?>> fields = List.of(
				new StringField(new StringFieldKey("hahah"), someStr0),
				new StringField(new StringFieldKey("moose"), someStr1),
				new UInt31Field(new UInt31FieldKey("integ"), testInt));

		Map<String, Field<?>> fieldMap = FieldSetTool.generateFieldMap(fields);
		Assert.assertEquals(fieldMap.size(), fields.size());
		Assert.assertNotNull(fieldMap.get("hahah"));
	}

	@Test
	public void testGetFieldDifferences(){
		StringFieldKey one = new StringFieldKey("one");
		StringFieldKey two = new StringFieldKey("two");
		BooleanFieldKey three = new BooleanFieldKey("three");
		LongFieldKey four = new LongFieldKey("four");
		DoubleFieldKey five = new DoubleFieldKey("five");
		UInt31FieldKey six = new UInt31FieldKey("six");
		Long sameRefLong = 123456789000L;

		List<Field<?>> left = List.of(
				new StringField(one, "help"),
				new StringField(two, "smite"),
				new BooleanField(three, true),
				new LongField(four, sameRefLong),
				new DoubleField(five, 5e6));
				// omitted six

		List<Field<?>> right = List.of(
				new StringField(one, "help"),
				new StringField(two, "two"),
				new BooleanField(three, null),
				new LongField(four, sameRefLong),
				// omitted five
				new UInt31Field(six, 55));

		Map<String, Pair<Field<?>, Field<?>>> diffs = FieldSetTool.getFieldDifferences(left, right);
		Pair<Field<?>, Field<?>> test;

		test = diffs.get(one.getName());
		Assert.assertNull(test);

		test = diffs.get(two.getName());
		Assert.assertNotNull(test);
		Assert.assertNotSame(test.getRight().getValue(), test.getLeft().getValue());

		test = diffs.get(three.getName());
		Assert.assertNotNull(test);
		Assert.assertNull(test.getRight().getValue());
		Assert.assertNotSame(test.getRight().getValue(), test.getLeft().getValue());

		test = diffs.get(four.getName());
		Assert.assertNull(test);

		test = diffs.get(five.getName());
		Assert.assertNotNull(test);
		Assert.assertNull(test.getRight());

		test = diffs.get(six.getName());
		Assert.assertNotNull(test);
		Assert.assertNull(test.getLeft());

		test = diffs.get("this test does not exist");
		Assert.assertNull(test);
	}

}