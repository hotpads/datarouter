package com.hotpads.datarouter.node.type.index.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

public class TestDatabeanWithManagedIndex extends BaseDatabean<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex>{

	private TestDatabeanWithManagedIndexKey key;
	private String b;
	private String c;

	public static class TestDatabeanWithManagedIndexFielder extends
			BaseDatabeanFielder<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex>{

		@Override
		public Class<? extends Fielder<TestDatabeanWithManagedIndexKey>> getKeyFielderClass(){
			return TestDatabeanWithManagedIndexKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndex databean){
			return FieldTool.createList(
					new StringField("b", databean.b, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField("c", databean.c, MySqlColumnType.MAX_LENGTH_VARCHAR)
					);
		}
		
	}
	
	public TestDatabeanWithManagedIndex(){
		this.key = new TestDatabeanWithManagedIndexKey();
	}
	
	public TestDatabeanWithManagedIndex(String a, String b, String c){
		this.key = new TestDatabeanWithManagedIndexKey(a);
		this.b = b;
		this.c = c;
	}
	
	@Override
	public Class<TestDatabeanWithManagedIndexKey> getKeyClass(){
		return TestDatabeanWithManagedIndexKey.class;
	}

	@Override
	public TestDatabeanWithManagedIndexKey getKey(){
		return key;
	}

	public String getB(){
		return b;
	}

	public String getC(){
		return c;
	}

	public String getA(){
		return key.getA();
	}

}
