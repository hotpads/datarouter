package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanWithManagedIndexByBarKey extends BasePrimaryKey<TestDatabeanWithManagedIndexByBarKey>{

	private String bar;

	public TestDatabeanWithManagedIndexByBarKey(){
		this(null);
	}

	public TestDatabeanWithManagedIndexByBarKey(String bar){
		this.bar = bar;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("bar", bar, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public String getBar(){
		return bar;
	}

}
