package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.Arrays;
import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanWithManagedIndexByCKey extends BasePrimaryKey<TestDatabeanWithManagedIndexByCKey>{

	private String c;
	
	public TestDatabeanWithManagedIndexByCKey(){
		this(null);
	}
	
	public TestDatabeanWithManagedIndexByCKey(String c){
		this.c = c;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("c", c, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public String getC(){
		return c;
	}

}
