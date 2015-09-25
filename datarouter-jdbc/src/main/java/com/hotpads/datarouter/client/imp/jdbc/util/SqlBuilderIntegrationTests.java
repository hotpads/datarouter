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
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class SqlBuilderIntegrationTests{

	private static final String TEST_TABLE = "TestTable";
	private static final String STRING_1 = "baz";
	private static final String STRING_2 = "degemer";
	private static final int INT_1 = 42;
	private static final int INT_2 = 24;
	private static final TestKey KEY_1 = new TestKey(INT_1, STRING_1);
	private static final TestKey KEY_2 = new TestKey(INT_2, STRING_2);
	private static final List<TestKey> ONE_KEY = Arrays.asList(KEY_1);
	private static final List<TestKey> TWO_KEYS = Arrays.asList(KEY_1, KEY_2);
	private static final String WHERE_KEY_1 = TestKey.FOO + "=" + INT_1 + " and " + TestKey.BAR + "='" + STRING_1 + "'";
	private static final String WHERE_KEY_2 = TestKey.FOO + "=" + INT_2 + " and " + TestKey.BAR + "='" + STRING_2 + "'";
	private static final Config config  = new Config().setLimit(10).setOffset(5);


	@Inject
	private JdbcFieldCodecFactory jdbcFieldCodecFactory;

	@Test
	public void testCount(){
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, TEST_TABLE, Collections.emptyList()),
				"select count(*) from " + TEST_TABLE);
		Assert.assertEquals(SqlBuilder.getCount(jdbcFieldCodecFactory, TEST_TABLE, ONE_KEY), "select count(*) from "
				+ TEST_TABLE + " where " + WHERE_KEY_1);
	}

	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testErrorOnMissingTableName(){
		SqlBuilder.getCount(jdbcFieldCodecFactory, null, Collections.emptyList());
	}

	@Test
	public void testGetAll(){
		String whereClause = "foo > 23";
		Assert.assertEquals(SqlBuilder.getAll(config, TEST_TABLE, KEY_1.getFields(), whereClause, KEY_1.getFields()),
				"select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE + " where "
						+ whereClause + " order by " + TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getAll(config, TEST_TABLE, KEY_1.getFields(), null, KEY_1.getFields()),
				"select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE + " order by "
						+ TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getAll(new Config().setLimit(10), TEST_TABLE, KEY_1.getFields(), null,
				KEY_1.getFields()), "select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE
						+ " order by " + TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 10");
		Assert.assertEquals(SqlBuilder.getAll(new Config().setOffset(5), TEST_TABLE, KEY_1.getFields(), null,
				KEY_1.getFields()), "select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE
						+ " order by " + TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 5, " + Integer.MAX_VALUE);
	}

	@Test
	public void testDeleteAll(){
		Assert.assertEquals(SqlBuilder.deleteAll(null, TEST_TABLE), "delete from " + TEST_TABLE);
	}

	@Test
	public void testGetMulti(){
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, TEST_TABLE, KEY_1.getFields(), ONE_KEY),
				"select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE + " where "
						+ WHERE_KEY_1 + " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, TEST_TABLE, KEY_1.getFields(), null),
				"select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE + " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getMulti(jdbcFieldCodecFactory, config, TEST_TABLE, KEY_1.getFields(), TWO_KEYS),
				"select " + FieldTool.getCsvColumnNames(KEY_1.getFields()) + " from " + TEST_TABLE + " where "
						+WHERE_KEY_1 + " or " + WHERE_KEY_2 + " limit 5, 10");
	}

	@Test
	public void testDeleteMulti(){
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, TEST_TABLE, null),
				"delete from " + TEST_TABLE + " limit 5, 10");
		Assert.assertEquals(SqlBuilder.deleteMulti(jdbcFieldCodecFactory, config, TEST_TABLE, ONE_KEY),
				"delete from " + TEST_TABLE + " where " + WHERE_KEY_1 + " limit 5, 10");
	}

	@Test
	public void testGetWithPrefixes(){
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, ONE_KEY, false,
				KEY_1.getFields()), "select * from " + TEST_TABLE + " where " + WHERE_KEY_1 + " order by " + TestKey.FOO
						+ " asc, " + TestKey.BAR + " asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, TWO_KEYS, false,
				KEY_1.getFields()), "select * from " + TEST_TABLE + " where " + WHERE_KEY_1 + " or " + WHERE_KEY_2
						+ " order by " + TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, null, false,
				null), "select * from " + TEST_TABLE + " limit 5, 10");

		List<TestKey> oneNullPrefix = Arrays.asList(new TestKey(null, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, oneNullPrefix,
				false, KEY_1.getFields()), "select * from " + TEST_TABLE + " order by " + TestKey.FOO + " asc, "
						+ TestKey.BAR + " asc limit 5, 10");
		List<TestKey> onePrefix = Arrays.asList(new TestKey(INT_1, null));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, onePrefix,
				false, KEY_1.getFields()), "select * from " + TEST_TABLE + " where " + TestKey.FOO + "=" + INT_1
						+ " order by " + TestKey.FOO + " asc, " + TestKey.BAR + " asc limit 5, 10");
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, ONE_KEY, true,
				KEY_1.getFields()), "select * from " + TEST_TABLE + " where " + TestKey.FOO + "=" + INT_1
						+ " and " + TestKey.BAR + " like '" + STRING_1 + "%' order by " + TestKey.FOO + " asc, "
						+ TestKey.BAR + " asc limit 5, 10");
		List<OtherKey> oneOtherKey = Arrays.asList(new OtherKey(STRING_1, INT_1));
		Assert.assertEquals(SqlBuilder.getWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, oneOtherKey,
				true, KEY_1.getFields()), "select * from " + TEST_TABLE + " where "  + TestKey.BAR + "='" + STRING_1
				+ "' and " + TestKey.FOO + "=" + INT_1 + " order by " + TestKey.FOO + " asc, " + TestKey.BAR
				+ " asc limit 5, 10");
	}

	@Test
	public void testDeleteWithPrefixes(){
		Assert.assertEquals(SqlBuilder.deleteWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, ONE_KEY, false),
				"delete from " + TEST_TABLE + " where " + WHERE_KEY_1 + " limit 5, 10");
		Assert.assertEquals(SqlBuilder.deleteWithPrefixes(jdbcFieldCodecFactory, config, TEST_TABLE, null, false),
				"delete from " + TEST_TABLE + " limit 5, 10");
	}

	@Test
	public void testGetInRange(){
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null, new Range<>(KEY_1),
				null), "select * from " + TEST_TABLE + " where ((" + TestKey.FOO + "=" + INT_1 + " and " + TestKey.BAR
						+ ">='" + STRING_1 + "') or (" + TestKey.FOO + ">" + INT_1 + ")) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<>(KEY_1, false, new TestKey(null, null), true), null), "select * from " + TEST_TABLE
						+ " where ((" + TestKey.FOO + "=" + INT_1 + " and " + TestKey.BAR + ">'" + STRING_1 + "') or ("
						+ TestKey.FOO + ">" + INT_1 + ")) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<TestKey>(null), null), "select * from " + TEST_TABLE + " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<>(new TestKey(null, null), new TestKey(null, null)), null), "select * from " + TEST_TABLE
						+ " limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<>(new TestKey(null, null), KEY_2), null), "select * from " + TEST_TABLE + " where (("
						+ TestKey.FOO + "<" + INT_2 + ") or (" + TestKey.FOO + "=" + INT_2 + " and " + TestKey.BAR
						+ "<'" + STRING_2 + "')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<>(null, KEY_2), null), "select * from " + TEST_TABLE + " where (("+ TestKey.FOO + "<" + INT_2
						+ ") or (" + TestKey.FOO + "=" + INT_2 + " and " + TestKey.BAR + "<'" + STRING_2
						+ "')) limit 5, 10");
		Assert.assertEquals(SqlBuilder.getInRange(jdbcFieldCodecFactory, config, TEST_TABLE, null,
				new Range<>(KEY_1, true, KEY_2, true), null), "select * from " + TEST_TABLE + " where ((" + TestKey.FOO
						+ "=" + INT_1 + " and " + TestKey.BAR + ">='" + STRING_1 + "') or (" + TestKey.FOO + ">" + INT_1
						+ ")) and ((" + TestKey.FOO + "<" + INT_2 + ") or (" + TestKey.FOO + "=" + INT_2 + " and "
						+ TestKey.BAR + "<='" + STRING_2 + "')) limit 5, 10");
	}

	@Test
	public void testAppendSqlUpdateClauses(){
		StringBuilder stringBuilder = new StringBuilder();
		SqlBuilder.appendSqlUpdateClauses(stringBuilder, KEY_1.getFields());
		Assert.assertEquals(stringBuilder.toString(), TestKey.FOO + "=?," + TestKey.BAR + "=?");
	}

	@SuppressWarnings("serial")
	private static class TestKey extends BaseKey<TestKey>{

		private static final String FOO = "foo";
		private static final String BAR = "bar";

		private final Integer foo;
		private final String bar;

		private TestKey(Integer foo, String bar){
			this.foo = foo;
			this.bar = bar;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
					new IntegerField(FOO, foo),
					new StringField(BAR, bar, 42));
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
