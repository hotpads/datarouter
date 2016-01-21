package com.hotpads.datarouter.test;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanKey extends BasePrimaryKey<TestDatabeanKey>{

	private String foo;

	public TestDatabeanKey(){
		this(null);
	}

	public TestDatabeanKey(String foo){
		this.foo = foo;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("foo", foo, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public String getFoo(){
		return foo;
	}

}
