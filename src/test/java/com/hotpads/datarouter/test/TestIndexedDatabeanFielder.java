package com.hotpads.datarouter.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public class TestIndexedDatabeanFielder extends TestDatabeanFielder{

	@Override
	public Map<String, List<Field<?>>> getIndexes(TestDatabean databean){
		Map<String, List<Field<?>>> indexes = new HashMap<>();
		indexes.put("byB", new TestDatabeanByBLookup(databean.getB()).getFields());
		indexes.put("byC", new TestDatabeanByCLookup(databean.getC()).getFields());
		return indexes;
	}
	
	@SuppressWarnings("serial")
	public static class TestDatabeanByBLookup extends BaseLookup<TestDatabeanKey>{

		private String b;
		
		public TestDatabeanByBLookup(String b){
			this.b = b;
		}
		
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(new StringField("b", b, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	
	}
	
	@SuppressWarnings("serial")
	public static class TestDatabeanByCLookup extends BaseLookup<TestDatabeanKey> implements UniqueKey<TestDatabeanKey>{

		private String c;
		
		public TestDatabeanByCLookup(String c){
			this.c = c;
		}
		
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(new StringField("c", c, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	
	}
	
}