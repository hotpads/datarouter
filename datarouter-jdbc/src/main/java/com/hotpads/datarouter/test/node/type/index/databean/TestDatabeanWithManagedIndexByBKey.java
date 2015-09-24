package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanWithManagedIndexByBKey extends BasePrimaryKey<TestDatabeanWithManagedIndexByBKey>{

	private String b;
	
	public TestDatabeanWithManagedIndexByBKey(){
		this(null);
	}
	
	public TestDatabeanWithManagedIndexByBKey(String b){
		this.b = b;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("b", b, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public String getB(){
		return b;
	}

}
