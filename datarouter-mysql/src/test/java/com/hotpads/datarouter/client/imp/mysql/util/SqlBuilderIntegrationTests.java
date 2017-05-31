package com.hotpads.datarouter.client.imp.mysql.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.mysql.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.EmptyMySqlCharacterSetCollationOpt;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCharacterSetCollationOpt;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class SqlBuilderIntegrationTests{

	private static final TestKey KEY_1 = new TestKey(42, "baz");
	private static final TestKey KEY_2 = new TestKey(24, "degemer");
	private static final TestKey KEY_3 = new TestKey(42, "mat");
	private static final TestKey KEY_NULL = new TestKey(null, null);

	private static final List<TestKey> ONE_KEY = Arrays.asList(KEY_1);
	private static final List<TestKey> ONE_KEY_NULL = Arrays.asList(KEY_NULL);
	private static final List<TestKey> TWO_KEYS = Arrays.asList(KEY_1, KEY_2);
	private static final Config config = new Config().setLimit(10).setOffset(5);

	private static final MySqlCharacterSetCollationOpt UTF8_BIN = getMySqlCharacterSetCollationOpt(
			MySqlCharacterSet.utf8, MySqlCollation.utf8_bin);

	private static MySqlCharacterSetCollationOpt getMySqlCharacterSetCollationOpt(MySqlCharacterSet characterSet,
			MySqlCollation collation){
		return new MySqlCharacterSetCollationOpt(){
			@Override
			public Optional<MySqlCharacterSet> getCharacterSetOpt(){
				return Optional.ofNullable(characterSet);
			}

			@Override
			public Optional<MySqlCollation> getCollationOpt(){
				return Optional.ofNullable(collation);
			}
		};
	}

	@Inject
	private JdbcFieldCodecFactory jdbcFieldCodecFactory;

	@Test
	public void testCount(){
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", Collections.emptyList(), new
				EmptyMySqlCharacterSetCollationOpt()), "select count(*) from TestTable");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, new
				EmptyMySqlCharacterSetCollationOpt()), "select count(*) from TestTable where foo=42 and "
				+ "bar='baz'");

		//test literal introducer
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, UTF8_BIN), "select "
				+ "count(*) from TestTable where foo=42 and bar=_utf8 'baz' COLLATE utf8_bin");
	}

	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void testErrorOnMissingTableName(){
		SqlBuilder.getCount(jdbcFieldCodecFactory, null, Collections.emptyList(), new
				EmptyMySqlCharacterSetCollationOpt());
	}

	@Test
	public void testGetAll(){
		String whereClause = "foo > 23";
		Assert.assertEquals(SqlBuilder.getAll(config, "TestTable", KEY_1.getFields(), whereClause, KEY_1.getFields()),
				"select foo, bar from TestTable where " + whereClause + " order by foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getAll(config, "TestTable", KEY_1.getFields(), null, KEY_1.getFields()),
				"select foo, bar from TestTable order by foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getAll(new Config().setLimit(10), "TestTable", KEY_1.getFields(), null,
				KEY_1.getFields()), "select foo, bar from TestTable order by foo asc, bar asc limit 10");
		Assert.assertEquals(SqlBuilder.getAll(new Config().setOffset(5), "TestTable", KEY_1.getFields(), null,
				KEY_1.getFields()), "select foo, bar from TestTable order by foo asc, bar asc limit 5, "
						+ Integer.MAX_VALUE);
	}

	@Test
	public void testDeleteAll(){
		Assert.assertEquals(SqlBuilder.deleteAll(null, "TestTable"), "delete from TestTable");
	}

	@Test
	public void testGetMulti(){
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(), ONE_KEY,
				new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where foo=42 and "
				+ "bar='baz' limit "
				+ "5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(), null,
				new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				TWO_KEYS, new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where foo=42 "
				+ "and bar='baz' or foo=24 and bar='degemer' limit 5, 10");

		//test literal introducer
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				TWO_KEYS, UTF8_BIN), "select foo, bar from TestTable where foo=42 and bar=_utf8 'baz' COLLATE utf8_bin"
				+ " or foo=24 and bar=_utf8 'degemer' COLLATE utf8_bin limit 5, 10");
	}

	@Test
	public void testDeleteMulti(){
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, "TestTable", null, new
				EmptyMySqlCharacterSetCollationOpt()), "delete from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, "TestTable", ONE_KEY, new
				EmptyMySqlCharacterSetCollationOpt()), "delete from TestTable where foo=42 and bar='baz' limit 5, "
				+ "10");

		//test literal introducer
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, "TestTable", ONE_KEY, UTF8_BIN),
				"delete from TestTable where foo=42 and bar=_utf8 'baz' COLLATE utf8_bin limit 5, 10");
	}

	@Test
	public void testGetWithPrefixes(){
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ONE_KEY, false, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar "
				+ "from TestTable where foo=42 and bar='baz' order by foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				TWO_KEYS, false, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar "
				+ "from TestTable where foo=42 and bar='baz' or foo=24 and bar='degemer' order by foo asc, bar asc "
				+ "limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				null, false, null, new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable "
				+ "limit 5, 10");

		List<TestKey> oneNullPrefix = Arrays.asList(new TestKey(null, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				oneNullPrefix, false, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, "
				+ "bar from TestTable order by foo asc, bar asc limit 5, 10");
		List<TestKey> onePrefix = Arrays.asList(new TestKey(42, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				onePrefix, false, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar "
				+ "from TestTable where foo=42 order by foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ONE_KEY, true, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar "
				+ "from TestTable where foo=42 and bar like 'baz%' order by foo asc, bar asc limit 5, 10");
		List<OtherKey> oneOtherKey = Arrays.asList(new OtherKey("baz", 42));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				oneOtherKey, true, KEY_1.getFields(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar "
				+ "from TestTable where bar='baz' and foo=42 order by foo asc, bar asc limit 5, 10");

		//test literal introducer
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				oneOtherKey, true, KEY_1.getFields(), UTF8_BIN), "select foo, bar from TestTable where bar=_utf8 'baz' "
				+ "COLLATE utf8_bin and foo=42 order by foo asc, bar asc limit 5, 10");
	}

	@Test
	public void testGetInRanges(){
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1)), null, Optional.empty(), new EmptyMySqlCharacterSetCollationOpt()),
				"select foo, bar from TestTable where ((foo=42 and bar>='baz') or (foo>42)) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, false, new TestKey(null, null), true)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where ((foo=42 and bar>'baz') or"
				+ " (foo>42)) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<TestKey>(null)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(new TestKey(null, null), new TestKey(null, null))), null, Optional.empty(),
				new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(new TestKey(null, null), KEY_2)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where ((foo<24) or (foo=24 and "
				+ "bar<'degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(null, KEY_2)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where ((foo<24) or (foo=24 and "
				+ "bar<'degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, true, KEY_2, true)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where ((foo=42 and bar>='baz') "
				+ "or (foo>42)) and ((foo<24) or (foo=24 and bar<='degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, true, KEY_3, true)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where (foo=42 and ((bar>='baz'))"
				+ " and ((bar<='mat'))) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, true, KEY_1, true)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where (foo=42 and bar='baz') "
				+ "limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, false, KEY_1, true)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where 0 limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(KEY_1, true, KEY_1, false)), null, Optional.empty(), new
				EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable where 0 limit 5, 10");

		//test force index and literal introducer
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(new TestKey(KEY_1.foo, null), true, KEY_1, true)), null,
				Optional.of("SomeIndex"), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from TestTable "
				+ "force index (SomeIndex) where (foo=42 and ((bar<='baz'))) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(new TestKey(KEY_1.foo, null), true, KEY_1, true)), null,
				Optional.of("SomeIndex"), UTF8_BIN), "select foo, bar from TestTable force index (SomeIndex) where "
				+ "(foo=42 and ((bar<=_utf8 'baz' COLLATE utf8_bin))) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				Arrays.asList(new Range<>(new TestKey(KEY_1.foo, null), true, KEY_1, true)), null,
				SqlBuilder.PRIMARY_KEY_INDEX_NAME_OPTIONAL, UTF8_BIN), "select foo, bar from TestTable force index "
				+ "(PRIMARY) where (foo=42 and ((bar<=_utf8 'baz' COLLATE utf8_bin))) limit 5, 10");

		List<Range<TestKey>> ranges = Arrays.asList(new Range<>(new TestKey(4, "a"), new TestKey(6, "c")), new Range<>(
				new TestKey(8, "a"), new TestKey(10, "c")));
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ranges, null, Optional.empty(), new EmptyMySqlCharacterSetCollationOpt()), "select foo, bar from "
				+ "TestTable where ((foo=4 and bar>='a') or (foo>4)) and ((foo<6) or (foo=6 and bar<'c')) or "
				+ "((foo=8 and bar>='a') or (foo>8)) and ((foo<10) or (foo=10 and bar<'c')) limit 5," + " 10");
	}

	@Test
	public void testAppendSqlUpdateClauses(){
		StringBuilder stringBuilder = new StringBuilder();
		SqlBuilder.appendSqlUpdateClauses(stringBuilder, KEY_1.getFields());
		Assert.assertEquals(stringBuilder.toString(), "foo=?,bar=?");
	}

	//introducer tests
	@Test
	public void testLiteralsCombinations(){
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, new
				EmptyMySqlCharacterSetCollationOpt()), SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(null, null)));

		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, UTF8_BIN), "select "
				+ "count(*) from TestTable where foo=42 and bar=_utf8 'baz' COLLATE utf8_bin");

		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(MySqlCharacterSet.utf8, null)), "select count(*) from TestTable where "
				+ "foo=42 and bar=_utf8 'baz'");

		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(null, MySqlCollation.utf8_bin)), "select count(*) from TestTable where"
				+ " foo=42 and bar='baz' COLLATE utf8_bin");
	}

	@Test
	public void testShouldIntroduce(){
		//covers non-introducible (int) and no charset/coollation
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, new
				EmptyMySqlCharacterSetCollationOpt()), "select count(*) from TestTable where foo=42 and bar='baz'");

		//covers all combinations of default charset/collation differing
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(JdbcConnectionPool.CHARACTER_SET_CONNECTION, JdbcConnectionPool
				.COLLATION_CONNECTION)), "select count(*) from TestTable where foo=42 and bar='baz'");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(null, null)), "select count(*) from TestTable where foo=42 and "
				+ "bar='baz'");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(null, JdbcConnectionPool.COLLATION_CONNECTION)), "select count(*) from"
				+ " TestTable where foo=42 and bar='baz'");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(JdbcConnectionPool.CHARACTER_SET_CONNECTION, null)), "select count(*) "
				+ "from TestTable where foo=42 and bar='baz'");

		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(JdbcConnectionPool.CHARACTER_SET_CONNECTION, MySqlCollation
				.utf8mb4_unicode_ci)), "select count(*) from TestTable where foo=42 and bar=_utf8mb4 'baz' COLLATE "
				+ "utf8mb4_unicode_ci");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY,
				getMySqlCharacterSetCollationOpt(MySqlCharacterSet.utf8, null)), "select count(*) from TestTable where "
				+ "foo=42 and bar=_utf8 'baz'");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY, UTF8_BIN), "select "
				+ "count(*) from TestTable where foo=42 and bar=_utf8 'baz' COLLATE utf8_bin");
	}

	@Test
	public void testGetSqlNameValuePairEscaped(){
		Assert.assertEquals(SqlBuilder.getSqlNameValuePairsEscaped(jdbcFieldCodecFactory, KEY_NULL.getFields(),
				UTF8_BIN), Arrays.asList("foo is null", "bar is null"));
		Assert.assertEquals(SqlBuilder.getSqlNameValuePairsEscaped(jdbcFieldCodecFactory, KEY_3.getFields(),
				UTF8_BIN), Arrays.asList("foo=42", "bar=_utf8 'mat' COLLATE utf8_bin"));
	}

	private static class TestKey extends BaseKey<TestKey>{

		private final Integer foo;
		private final String bar;

		private TestKey(Integer foo, String bar){
			this.foo = foo;
			this.bar = bar;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(//TODO fix before committing
					new IntegerField("foo", foo),
					new StringField("bar", bar, 42));
		}

	}

	private static class OtherKey extends TestKey{

		private OtherKey(String bar, Integer foo){
			super(foo, bar);
		}

		@Override
		public List<Field<?>> getFields(){
			List<Field<?>> fields = super.getFields();
			Collections.reverse(fields);
			return fields;
		}

	}
}
