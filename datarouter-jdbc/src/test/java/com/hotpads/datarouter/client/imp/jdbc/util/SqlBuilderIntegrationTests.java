package com.hotpads.datarouter.client.imp.jdbc.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class SqlBuilderIntegrationTests{

	private static final TestKey KEY_1 = new TestKey(42, "baz");
	private static final TestKey KEY_2 = new TestKey(24, "degemer");
	private static final TestKey KEY_3 = new TestKey(42, "mat");
	private static final List<TestKey> ONE_KEY = Arrays.asList(KEY_1);
	private static final List<TestKey> TWO_KEYS = Arrays.asList(KEY_1, KEY_2);
	private static final Config config  = new Config().setLimit(10).setOffset(5);


	@Inject
	private JdbcFieldCodecFactory jdbcFieldCodecFactory;

	@Test
	public void testCount(){
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", Collections.emptyList()),
				"select count(*) from TestTable");
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, "TestTable", ONE_KEY), "select count(*) from "
				+ "TestTable where foo=42 and bar='baz'");
	}

	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testErrorOnMissingTableName(){
		SqlBuilder.getCount(jdbcFieldCodecFactory, null, Collections.emptyList());
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
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(), ONE_KEY),
				"select foo, bar from TestTable where foo=42 and bar='baz' limit 5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(), null),
				"select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				TWO_KEYS), "select foo, bar from TestTable where foo=42 and bar='baz' or foo=24 and bar='degemer' "
						+ "limit 5, 10");
	}

	@Test
	public void testDeleteMulti(){
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, "TestTable", null),
				"delete from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, "TestTable", ONE_KEY),
				"delete from TestTable where foo=42 and bar='baz' limit 5, 10");
	}

	@Test
	public void testGetWithPrefixes(){
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ONE_KEY, false, KEY_1.getFields()), "select foo, bar from TestTable where foo=42 and bar='baz' order by"
						+ " foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				TWO_KEYS, false, KEY_1.getFields()), "select foo, bar from TestTable where foo=42 and bar='baz' "
						+ "or foo=24 and bar='degemer' order by foo asc, bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				null, false, null), "select foo, bar from TestTable limit 5, 10");

		List<TestKey> oneNullPrefix = Arrays.asList(new TestKey(null, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				oneNullPrefix, false, KEY_1.getFields()), "select foo, bar from TestTable order by foo asc, bar asc "
						+ "limit 5, 10");
		List<TestKey> onePrefix = Arrays.asList(new TestKey(42, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				onePrefix, false, KEY_1.getFields()), "select foo, bar from TestTable where foo=42 order by foo asc, "
						+ "bar asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ONE_KEY, true, KEY_1.getFields()), "select foo, bar from TestTable where foo=42 and bar like 'baz%' "
						+ "order by foo asc, bar asc limit 5, 10");
		List<OtherKey> oneOtherKey = Arrays.asList(new OtherKey("baz", 42));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				oneOtherKey, true, KEY_1.getFields()), "select foo, bar from TestTable where bar='baz' and foo=42 order"
						+ " by foo asc, bar asc limit 5, 10");
	}

	@Test
	public void testDeleteWithPrefixes(){
		Assert.assertEquals(SqlBuilder.deleteWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", ONE_KEY, false),
				"delete from TestTable where foo=42 and bar='baz' limit 5, 10");
		Assert.assertEquals(SqlBuilder.deleteWithPrefixes(jdbcFieldCodecFactory, config, "TestTable", null, false),
				"delete from TestTable limit 5, 10");
	}

	@Test
	public void testGetInRange(){
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(KEY_1), null), "select foo, bar from TestTable where ((foo=42 and bar>='baz') or (foo>42))"
						+ " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(KEY_1, false, new TestKey(null, null), true), null), "select foo, bar from TestTable where "
						+ "((foo=42 and bar>'baz') or (foo>42)) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<TestKey>(null), null), "select foo, bar from TestTable limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(new TestKey(null, null), new TestKey(null, null)), null), "select foo, bar from TestTable"
						+ " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(new TestKey(null, null), KEY_2), null), "select foo, bar from TestTable where ((foo<24) or "
						+ "(foo=24 and bar<'degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(null, KEY_2), null), "select foo, bar from TestTable where ((foo<24) or (foo=24 and "
						+ "bar<'degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(KEY_1, true, KEY_2, true), null), "select foo, bar from TestTable where ((foo=42 and "
						+ "bar>='baz') or (foo>42)) and ((foo<24) or (foo=24 and bar<='degemer')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(KEY_1, true, KEY_3, true), null), "select foo, bar from TestTable where (foo=42 and "
						+ "((bar>='baz')) and ((bar<='mat'))) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(KEY_1, true, KEY_1, true), null), "select foo, bar from TestTable where (foo=42 and "
						+ "bar='baz') limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				new Range<>(new TestKey(KEY_1.foo, null), true, KEY_1, true), null), "select foo, bar from TestTable "
						+ "where (foo=42 and ((bar<='baz'))) limit 5, 10");
	}

	@Test
	public void testGetInRanges(){
		List<Range<TestKey>> ranges = Arrays.asList(new Range<>(new TestKey(4, "a"), new TestKey(6, "c")), new Range<>(
				new TestKey(8, "a"), new TestKey(10, "c")));
		Assert.assertEquals(SqlBuilder.getInRanges(jdbcFieldCodecFactory, config, "TestTable", KEY_1.getFields(),
				ranges, null), "select foo, bar from TestTable where ((foo=4 and bar>='a') or (foo>4)) and ((foo<6) or"
						+ " (foo=6 and bar<'c')) or ((foo=8 and bar>='a') or (foo>8)) and ((foo<10) or (foo=10 and "
						+ "bar<'c')) limit 5," + " 10");
	}

	@Test
	public void testAppendSqlUpdateClauses(){
		StringBuilder stringBuilder = new StringBuilder();
		SqlBuilder.appendSqlUpdateClauses(stringBuilder, KEY_1.getFields());
		Assert.assertEquals(stringBuilder.toString(), "foo=?,bar=?");
	}

	@SuppressWarnings("serial")
	private static class TestKey extends BaseKey<TestKey>{

		private final Integer foo;
		private final String bar;

		private TestKey(Integer foo, String bar){
			this.foo = foo;
			this.bar = bar;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
					new IntegerField("foo", foo),
					new StringField("bar", bar, 42));
		}

	}

	@SuppressWarnings("serial")
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
