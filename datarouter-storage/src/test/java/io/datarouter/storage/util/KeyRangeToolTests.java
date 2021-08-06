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
package io.datarouter.storage.util;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.tuple.Range;

public class KeyRangeToolTests{

	private static final Integer foo = 4;

	@Test
	public void testForPrefixWithRegularWildcard(){
		String barPrefix = "degemer";
		Range<TestKey> range = KeyRangeTool.forPrefixWithWildcard(barPrefix, prefix -> new TestKey(foo, prefix, null));
		Assert.assertTrue(range.getStartInclusive());
		Assert.assertFalse(range.getEndInclusive());
		Assert.assertEquals(range.getStart().foo, foo);
		Assert.assertEquals(range.getStart().bar, barPrefix);
		Assert.assertEquals(range.getStart().baz, null);
		Assert.assertEquals(range.getEnd().foo, foo);
		Assert.assertEquals(range.getEnd().bar, "degemes");
		Assert.assertEquals(range.getEnd().baz, null);
		Assert.assertTrue(range.contains(new TestKey(foo, "degemer", null)));
		Assert.assertTrue(range.contains(new TestKey(foo, "degemermat", 53L)));
		Assert.assertFalse(range.contains(new TestKey(foo, "degemeola", null)));
	}

	@Test
	public void testForPrefixWithNullWildcard(){
		Assert.assertEquals(KeyRangeTool.forPrefixWithWildcard(null, prefix -> new TestKey(foo, prefix, null)),
				KeyRangeTool.forPrefix(new TestKey(foo, null, null)));
	}

	@Test
	public void testIncrementLastCharWithNullString(){
		Assert.assertNull(KeyRangeTool.incrementLastChar(null));
	}

	@Test
	public void testIncrementLastCharWithEmptyString(){
		Assert.assertEquals(KeyRangeTool.incrementLastChar(""), String.valueOf('\u0000'));
	}

	@Test
	public void testIncrementLastCharEdgeCase(){
		String input = "foo" + Character.MAX_VALUE;
		String expected = input + Character.MIN_VALUE;
		Assert.assertEquals(KeyRangeTool.incrementLastChar(input), expected);
	}

	private static class TestKey extends BaseRegularPrimaryKey<TestKey>{

		private Integer foo;
		private String bar;
		private Long baz;

		public TestKey(Integer foo, String bar, Long baz){
			this.foo = foo;
			this.bar = bar;
			this.baz = baz;
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(
					new IntegerField(new IntegerFieldKey("foo"), foo),
					new StringField(new StringFieldKey("bar"), bar),
					new LongField(new LongFieldKey("baz"), baz));
		}

	}

}
