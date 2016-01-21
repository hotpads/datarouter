package com.hotpads.datarouter.test;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;

public class TestDatabeanFielder extends BaseDatabeanFielder<TestDatabeanKey, TestDatabean>{

	public TestDatabeanFielder(){
		super(TestDatabeanKey.class);
	}

	@Override
	public List<Field<?>> getNonKeyFields(TestDatabean databean){
		return Arrays.asList(
				new StringField("bar", databean.getBar(), MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField("baz", databean.getBaz(), MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

}