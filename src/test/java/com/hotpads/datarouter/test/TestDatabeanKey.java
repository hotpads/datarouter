package com.hotpads.datarouter.test;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanKey extends BasePrimaryKey<TestDatabeanKey>{

	private String a;
	
	public TestDatabeanKey(){
		this(null);
	}
	
	public TestDatabeanKey(String a){
		this.a = a;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(new StringField("a", a, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public String getA(){
		return a;
	}

}
