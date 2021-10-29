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
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;

public class PrimaryKeyPercentCodecToolTests{

	private static SortedBeanKey
			SBK_0 = new SortedBeanKey("abc", "def", 3, "ghi"),
			SBK_1 = new SortedBeanKey("%ab/", "d&^f", 3, "g_-hi"),
			SBK_NULL = new SortedBeanKey(null, null, 0, "c"),
			SBK_EMPTY_STRING = new SortedBeanKey("", "", 0, "c");

	private static List<SortedBeanKey> SBK_MULTI = List.of(SBK_0, SBK_1);

	@Test
	public void testSimpleNumericPk(){
		String id = "355";
		TestKey pk = new TestKey(id);
		String encoded = PrimaryKeyPercentCodecTool.encode(pk);
		TestKey decoded = PrimaryKeyPercentCodecTool.decode(TestKey::new, encoded);
		Assert.assertEquals(decoded.getId(), id);
	}

	@Test
	public void testMultiNumericPk(){
		final char delimiter = ',';
		List<String> ids = List.of("23", "52", "103");
		List<TestKey> pks = Scanner.of(ids).map(TestKey::new).list();
		String encoded = PrimaryKeyPercentCodecTool.encodeMulti(pks, delimiter);
		List<TestKey> decodedPks = PrimaryKeyPercentCodecTool.decodeMulti(TestKey::new, delimiter, encoded);
		List<String> decodedIds = Scanner.of(decodedPks).map(TestKey::getId).list();
		Assert.assertEquals(ids, decodedIds);
	}

	@Test
	public void testStringPk(){
		String encoded = PrimaryKeyPercentCodecTool.encode(SBK_0);
		SortedBeanKey decoded = PrimaryKeyPercentCodecTool.decode(SortedBeanKey::new, encoded);
		Assert.assertEquals(decoded, SBK_0);
	}

	@Test
	public void testStringPkWithReservedCharacters(){
		String encoded = PrimaryKeyPercentCodecTool.encode(SBK_1);
		SortedBeanKey decoded = PrimaryKeyPercentCodecTool.decode(SortedBeanKey::new, encoded);
		Assert.assertEquals(decoded, SBK_1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidDelimiter(){
		PrimaryKeyPercentCodecTool.encodeMulti(SBK_MULTI, '/');
	}

	@Test
	public void testEncodeMulti(){
		char delimiter = ',';
		String encoded = PrimaryKeyPercentCodecTool.encodeMulti(SBK_MULTI, delimiter);
		List<SortedBeanKey> decoded = PrimaryKeyPercentCodecTool.decodeMulti(SortedBeanKey::new, delimiter, encoded);
		Assert.assertEquals(decoded, SBK_MULTI);
	}

	@Test
	public void testNullsBecomeEmptyStrings(){
		String encoded = PrimaryKeyPercentCodecTool.encode(SBK_NULL);
		SortedBeanKey decoded = PrimaryKeyPercentCodecTool.decode(SortedBeanKey::new, encoded);
		Assert.assertEquals(decoded, SBK_EMPTY_STRING);
	}

	public static class TestKey extends BaseRegularPrimaryKey<TestKey>{

		public String id;

		public static class FieldKeys{
			public static final StringFieldKey id = new StringFieldKey("id");
		}

		public TestKey(){
		}

		public TestKey(String id){
			this.id = id;
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.id, id));
		}

		public String getId(){
			return id;
		}

	}

}
