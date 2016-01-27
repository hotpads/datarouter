package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TestDatabeanWithManagedIndexByBazKey extends BasePrimaryKey<TestDatabeanWithManagedIndexByBazKey>{

	private String baz;

	public TestDatabeanWithManagedIndexByBazKey(){
		this(null);
	}

	public TestDatabeanWithManagedIndexByBazKey(String baz){
		this.baz = baz;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("baz", baz, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public String getC(){
		return baz;
	}

}
