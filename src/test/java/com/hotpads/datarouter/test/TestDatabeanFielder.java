package com.hotpads.datarouter.test;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

public class TestDatabeanFielder extends
		BaseDatabeanFielder<TestDatabeanKey, TestDatabean>{

	@Override
	public Class<? extends Fielder<TestDatabeanKey>> getKeyFielderClass(){
		return TestDatabeanKey.class;
	}

	@Override
	public List<Field<?>> getNonKeyFields(TestDatabean databean){
		return FieldTool.createList(
				new StringField("b", databean.getB(), MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField("c", databean.getC(), MySqlColumnType.MAX_LENGTH_VARCHAR)
				);
	}
	
}