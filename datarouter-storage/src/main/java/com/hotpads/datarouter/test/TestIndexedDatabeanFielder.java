package com.hotpads.datarouter.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class TestIndexedDatabeanFielder extends TestDatabeanFielder{

	@Override
	public Map<String, List<Field<?>>> getIndexes(TestDatabean databean){
		Map<String, List<Field<?>>> indexes = new HashMap<>();
		indexes.put("byBar", new TestDatabeanByBarLookup(databean.getBar()).getFields());
		indexes.put("byBaz", new TestDatabeanByBazLookup(databean.getBaz()).getFields());
		return indexes;
	}

	@SuppressWarnings("serial")
	public static class TestDatabeanByBarLookup extends BaseLookup<TestDatabeanKey>{

		private String bar;

		public TestDatabeanByBarLookup(String bar){
			this.bar = bar;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(new StringField("bar", bar, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	@SuppressWarnings("serial")
	public static class TestDatabeanByBazLookup extends BaseLookup<TestDatabeanKey>
	implements UniqueKey<TestDatabeanKey>{

		private String baz;

		public TestDatabeanByBazLookup(String baz){
			this.baz = baz;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(new StringField("baz", baz, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

}