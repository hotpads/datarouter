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
package io.datarouter.storage.util;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.util.iterable.IterableTool;

public class PrimaryKeyPercentCodecTests{

	private static SortedBeanKey
			SBK_0 = new SortedBeanKey("abc", "def", 3, "ghi"),
			SBK_1 = new SortedBeanKey("%ab/", "d&^f", 3, "g_-hi"),
			SBK_NULL = new SortedBeanKey(null, null, 0, "c"),
			SBK_EMPTY_STRING = new SortedBeanKey("", "", 0, "c");

	private static List<SortedBeanKey> SBK_MULTI = Arrays.asList(SBK_0, SBK_1);

	@Test
	public void testSimpleNumericPk(){
		String id = "355";
		TestKey pk = new TestKey(id);
		String encoded = PrimaryKeyPercentCodec.encode(pk);
		TestKey decoded = PrimaryKeyPercentCodec.decode(TestKey.class, encoded);
		Assert.assertEquals(decoded.getId(), id);
	}

	@Test
	public void testMultiNumericPk(){
		final char delimiter = ',';
		List<String> ids = Arrays.asList("23", "52", "103");
		List<TestKey> pks = IterableTool.map(ids, TestKey::new);
		String encoded = PrimaryKeyPercentCodec.encodeMulti(pks, delimiter);
		List<TestKey> decodedPks = PrimaryKeyPercentCodec.decodeMulti(TestKey.class, delimiter, encoded);
		List<String> decodedIds = IterableTool.map(decodedPks, TestKey::getId);
		Assert.assertEquals(ids, decodedIds);
	}

	@Test
	public void testStringPk(){
		String encoded = PrimaryKeyPercentCodec.encode(SBK_0);
		SortedBeanKey decoded = PrimaryKeyPercentCodec.decode(SortedBeanKey.class, encoded);
		Assert.assertEquals(decoded, SBK_0);
	}

	@Test
	public void testStringPkWithReservedCharacters(){
		String encoded = PrimaryKeyPercentCodec.encode(SBK_1);
		SortedBeanKey decoded = PrimaryKeyPercentCodec.decode(SortedBeanKey.class, encoded);
		Assert.assertEquals(decoded, SBK_1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidDelimiter(){
		PrimaryKeyPercentCodec.encodeMulti(SBK_MULTI, '/');
	}

	@Test
	public void testEncodeMulti(){
		final char delimiter = ',';
		String encoded = PrimaryKeyPercentCodec.encodeMulti(SBK_MULTI, delimiter);
		List<SortedBeanKey> decoded = PrimaryKeyPercentCodec.decodeMulti(SortedBeanKey.class, delimiter, encoded);
		Assert.assertEquals(decoded, SBK_MULTI);
	}

	@Test
	public void testNullsBecomeEmptyStrings(){
		String encoded = PrimaryKeyPercentCodec.encode(SBK_NULL);
		SortedBeanKey decoded = PrimaryKeyPercentCodec.decode(SortedBeanKey.class, encoded);
		Assert.assertEquals(decoded, SBK_EMPTY_STRING);
	}

	private static class TestKey extends BaseRegularPrimaryKey<TestKey>{

		public String id;

		public static class FieldKeys{
			public static final StringFieldKey id = new StringFieldKey("id");
		}

		@SuppressWarnings("unused")
		public TestKey(){
		}

		public TestKey(String id){
			this.id = id;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(new StringField(FieldKeys.id, id));
		}

		public String getId(){
			return id;
		}

	}
}