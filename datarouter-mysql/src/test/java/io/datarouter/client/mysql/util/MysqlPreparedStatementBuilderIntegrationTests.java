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
package io.datarouter.client.mysql.util;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions.MysqlTableOptionsBuilder;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.key.BaseKey;
import io.datarouter.storage.config.Config;
import io.datarouter.util.tuple.Range;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class MysqlPreparedStatementBuilderIntegrationTests{

	private static final Config CONFIG = new Config().setLimit(10).setOffset(5);

	private static final MysqlTableOptions UTF8_BIN = new MysqlTableOptionsBuilder()
			.withCharacterSet(MysqlCharacterSet.utf8)
			.withCollation(MysqlCollation.utf8_bin)
			.build();
	private static final MysqlTableOptions CONNECTION_OPTIONS = new MysqlTableOptionsBuilder()
			.withCharacterSet(MysqlConnectionPoolHolder.CHARACTER_SET_CONNECTION)
			.withCollation(MysqlConnectionPoolHolder.COLLATION_CONNECTION)
			.build();

	private static final TestKey KEY_1 = new TestKey(42, "baz");
	private static final TestKey KEY_2 = new TestKey(24, "degemer");
	private static final TestKey KEY_3 = new TestKey(24, "mat");

	private static final List<TestKey> ONE_KEY = Arrays.asList(KEY_1);
	private static final List<TestKey> TWO_KEYS = Arrays.asList(KEY_1, KEY_2);

	@Inject
	private MysqlPreparedStatementBuilder builder;

	@Test
	public void testGetWithPrefixes(){
		Assert.assertEquals(builder.getWithPrefixes(CONFIG, "TestTable", SqlBuilder.PRIMARY_KEY_INDEX_NAME, KEY_1
				.getFields(), TWO_KEYS, KEY_1.getFields(), CONNECTION_OPTIONS).getSql().toString(), "select foo, bar "
				+ "from TestTable force index (PRIMARY) where foo=? and bar=? or foo=? and bar=? order by foo asc, bar "
				+ "asc limit 5, 10");
		List<TestKey> oneNullPrefix = Arrays.asList(new TestKey(null, null));
		Assert.assertEquals(builder.getWithPrefixes(CONFIG, "TestTable", SqlBuilder.PRIMARY_KEY_INDEX_NAME, KEY_1
				.getFields(), oneNullPrefix, KEY_1.getFields(), CONNECTION_OPTIONS).getSql().toString(), "select foo, "
				+ "bar from TestTable force index (PRIMARY) order by foo asc, bar asc limit 5, 10");
		List<TestKey> onePrefix = Arrays.asList(new TestKey(42, null));
		Assert.assertEquals(builder.getWithPrefixes(CONFIG, "TestTable", SqlBuilder.PRIMARY_KEY_INDEX_NAME, KEY_1
				.getFields(), onePrefix, KEY_1.getFields(), CONNECTION_OPTIONS).getSql().toString(), "select foo, bar "
				+ "from TestTable force index (PRIMARY) where foo=? order by foo asc, bar asc limit 5, 10");
	}

	@Test
	public void testGetMulti(){
		Assert.assertEquals(builder.getMulti(CONFIG, "TestTable", KEY_1.getFields(), ONE_KEY, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable where foo=? and bar=? limit 5, 10");
		Assert.assertEquals(builder.getMulti(CONFIG, "TestTable", KEY_1.getFields(), null, CONNECTION_OPTIONS).getSql()
				.toString(), "select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(builder.getMulti(CONFIG, "TestTable", KEY_1.getFields(), TWO_KEYS, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable where foo=? and bar=? or foo=? and bar=? "
				+ "limit 5, 10");
		Assert.assertEquals(builder.getMulti(CONFIG, "TestTable", KEY_1.getFields(), TWO_KEYS, UTF8_BIN).getSql()
				.toString(), "select foo, bar from TestTable where foo=? and bar=_utf8 ? COLLATE utf8_bin or foo=? "
				+ "and bar=_utf8 ? COLLATE utf8_bin limit 5, 10");
	}

	@Test
	public void testDeleteMulti(){
		Assert.assertEquals(builder.deleteMulti(CONFIG, "TestTable", null, CONNECTION_OPTIONS).getSql().toString(),
				"delete from TestTable limit 5, 10");
		Assert.assertEquals(builder.deleteMulti(CONFIG, "TestTable", ONE_KEY, CONNECTION_OPTIONS).getSql().toString(),
				"delete from TestTable where foo=? and bar=? limit 5, 10");
	}

	@Test
	public void testGetInRanges(){
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_1)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS).getSql().toString(),
				"select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>=?) or (foo>?)) limit 5, "
				+ "10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(null,
				KEY_1)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS).getSql().toString(),
				"select foo, bar from TestTable force index (PRIMARY) where ((foo<?) or (foo=? and bar<?)) limit 5, "
				+ "10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(
				new Range<TestKey>(null, null)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_1), new Range<>(KEY_2)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>=?) "
				+ "or (foo>?)) or ((foo=? and bar>=?) or (foo>?)) limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_2, KEY_3)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where (foo=? and ((bar>=?))"
				+ " and ((bar<?))) limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_2, true, KEY_2, true)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where (foo=? and bar=?)"
				+ " limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				new TestKey(null, null), KEY_1)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where ((foo<?) or (foo=? "
				+ "and bar<?)) limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_1, false, KEY_2, true)), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>?) "
				+ "or (foo>?)) and ((foo<?) or (foo=? and bar<=?)) limit 5, 10");
		Assert.assertEquals(builder.getInRanges(CONFIG, "TestTable", KEY_1.getFields(), Arrays.asList(new Range<>(
				KEY_1, new TestKey(null, null))), null, SqlBuilder.PRIMARY_KEY_INDEX_NAME, CONNECTION_OPTIONS)
				.getSql().toString(), "select foo, bar from TestTable force index (PRIMARY) where ((foo=? "
				+ "and bar>=?) or (foo>?)) limit 5, 10");
	}

	private static class TestKey extends BaseKey<TestKey>{

		public static class FieldKeys{
			public static final IntegerFieldKey foo = new IntegerFieldKey("foo");
			public static final StringFieldKey bar = new StringFieldKey("bar").withSize(42);
		}

		private final Integer foo;
		private final String bar;

		private TestKey(Integer foo, String bar){
			this.foo = foo;
			this.bar = bar;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
					new IntegerField(FieldKeys.foo, foo),
					new StringField(FieldKeys.bar, bar));
		}

	}

}
