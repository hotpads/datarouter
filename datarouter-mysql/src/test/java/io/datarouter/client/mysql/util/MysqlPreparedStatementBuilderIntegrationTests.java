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
package io.datarouter.client.mysql.util;

import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
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

	private static final MysqlLiveTableOptions UTF8_BIN_OPTIONS = new MysqlLiveTableOptions(
			MysqlCharacterSet.utf8,
			MysqlCollation.utf8_bin);
	private static final MysqlLiveTableOptions CONNECTION_OPTIONS = new MysqlLiveTableOptions(
			MysqlConnectionPoolHolder.CHARACTER_SET_CONNECTION,
			MysqlConnectionPoolHolder.COLLATION_CONNECTION);

	private static final TestKey KEY_1 = new TestKey(42, "baz");
	private static final TestKey KEY_2 = new TestKey(24, "degemer");
	private static final TestKey KEY_3 = new TestKey(24, "mat");

	private static final List<TestKey> ONE_KEY = List.of(KEY_1);
	private static final List<TestKey> TWO_KEYS = List.of(KEY_1, KEY_2);

	@Inject
	private MysqlSqlFactory sqlFactory;

	private MysqlSql makeSql(){
		return sqlFactory.createSql(CONNECTION_OPTIONS, false);
	}

	private MysqlSql makeUtf8BinSql(){
		return sqlFactory.createSql(UTF8_BIN_OPTIONS, false);
	}

	@Test
	public void testGetWithPrefixes(){
		String actual1 = makeSql().getWithPrefixes(
				"TestTable",
				CONFIG,
				MysqlTool.PRIMARY_KEY_INDEX_NAME,
				KEY_1.getFields(),
				TWO_KEYS,
				KEY_1.getFields())
				.toString();
		Assert.assertEquals(actual1, "select foo, bar from TestTable force index (PRIMARY) "
				+ "where foo=? and bar=? or foo=? and bar=? order by foo asc, bar asc limit 5, 10");

		List<TestKey> oneNullPrefix = List.of(new TestKey(null, null));
		String actual2 = makeSql().getWithPrefixes(
				"TestTable",
				CONFIG,
				MysqlTool.PRIMARY_KEY_INDEX_NAME,
				KEY_1.getFields(),
				oneNullPrefix,
				KEY_1.getFields())
				.toString();
		Assert.assertEquals(actual2, "select foo, "
				+ "bar from TestTable force index (PRIMARY) order by foo asc, bar asc limit 5, 10");

		List<TestKey> onePrefix = List.of(new TestKey(42, null));
		var actual3 = makeSql().getWithPrefixes(
				"TestTable",
				CONFIG,
				MysqlTool.PRIMARY_KEY_INDEX_NAME,
				KEY_1.getFields(),
				onePrefix,
				KEY_1.getFields())
				.toString();
		Assert.assertEquals(actual3, "select foo, bar "
				+ "from TestTable force index (PRIMARY) where foo=? order by foo asc, bar asc limit 5, 10");
	}

	@Test
	public void testGetMulti(){
		String actual1 = makeSql().getMulti(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				ONE_KEY,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual1, "select foo, bar from TestTable force index (PRIMARY) where foo=? and bar=? limit"
				+ " 5, 10");

		String actual2 = makeSql().getMulti(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual2, "select foo, bar from TestTable force index (PRIMARY) limit 5, 10");

		String actual3 = makeSql().getMulti(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				TWO_KEYS,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual3,
				"select foo, bar from TestTable force index (PRIMARY) where foo=? and bar=? or foo=? and bar=? limit 5,"
						+ " 10");
		String actual4 = makeUtf8BinSql().getMulti(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				TWO_KEYS,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual4,
				"select foo, bar from TestTable force index (PRIMARY) where foo=? and bar=_utf8 ? COLLATE utf8_bin or "
						+ "foo=? and bar=_utf8 ? COLLATE utf8_bin limit 5, 10");
	}

	@Test
	public void testDeleteMulti(){
		String actual1 = makeSql().deleteMulti("TestTable", CONFIG, null).toString();
		Assert.assertEquals(actual1, "delete from TestTable limit 5, 10");

		String actual2 = makeSql().deleteMulti("TestTable", CONFIG, ONE_KEY).toString();
		Assert.assertEquals(actual2, "delete from TestTable where foo=? and bar=? limit 5, 10");
	}

	@Test
	public void testGetInRanges(){
		String actual1 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_1)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual1,
				"select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>=?) or (foo>?)) limit 5, "
				+ "10");

		String actual2 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(null, KEY_1)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual2,
				"select foo, bar from TestTable force index (PRIMARY) where ((foo<?) or (foo=? and bar<?)) limit 5, "
				+ "10");

		String actual3 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<TestKey>(null, null)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual3, "select foo, bar from TestTable force index (PRIMARY) limit 5, 10");

		Assert.assertEquals(makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_1), new Range<>(KEY_2)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString(),
				"select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>=?) "
				+ "or (foo>?)) or ((foo=? and bar>=?) or (foo>?)) limit 5, 10");

		String actual4 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_2, KEY_3)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual4,
				"select foo, bar from TestTable force index (PRIMARY) where (foo=? and ((bar>=?))"
				+ " and ((bar<?))) limit 5, 10");

		String actual5 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_2, true, KEY_2, true)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual5,
				"select foo, bar from TestTable force index (PRIMARY) where (foo=? and bar=?)"
				+ " limit 5, 10");

		String actual6 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(new TestKey(null, null), KEY_1)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual6,
				"select foo, bar from TestTable force index (PRIMARY) where ((foo<?) or (foo=? "
				+ "and bar<?)) limit 5, 10");

		String actual7 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_1, false, KEY_2, true)),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual7,
				"select foo, bar from TestTable force index (PRIMARY) where ((foo=? and bar>?) "
				+ "or (foo>?)) and ((foo<?) or (foo=? and bar<=?)) limit 5, 10");

		String actual8 = makeSql().getInRanges(
				"TestTable",
				CONFIG,
				KEY_1.getFields(),
				List.of(new Range<>(KEY_1, new TestKey(null, null))),
				null,
				MysqlTool.PRIMARY_KEY_INDEX_NAME)
				.toString();
		Assert.assertEquals(actual8, "select foo, bar from TestTable force index (PRIMARY) where ((foo=? "
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
			return List.of(new IntegerField(FieldKeys.foo, foo), new StringField(FieldKeys.bar, bar));
		}

	}

}
