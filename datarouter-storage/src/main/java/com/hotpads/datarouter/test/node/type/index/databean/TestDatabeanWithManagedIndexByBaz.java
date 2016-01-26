package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;

public class TestDatabeanWithManagedIndexByBaz
extends BaseDatabean<TestDatabeanWithManagedIndexByBazKey, TestDatabeanWithManagedIndexByBaz>
implements MultiIndexEntry<
		TestDatabeanWithManagedIndexByBazKey,
		TestDatabeanWithManagedIndexByBaz,
		TestDatabeanKey,
		TestDatabean>{

	private TestDatabeanWithManagedIndexByBazKey key;
	private String foo;

	public static class TestDatabeanWithManagedIndexByCFielder
	extends BaseDatabeanFielder<TestDatabeanWithManagedIndexByBazKey, TestDatabeanWithManagedIndexByBaz>{

		public TestDatabeanWithManagedIndexByCFielder(){
			super(TestDatabeanWithManagedIndexByBazKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndexByBaz databean){
			return Arrays.asList(new StringField("foo", databean.foo, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	public TestDatabeanWithManagedIndexByBaz(){
		this.key = new TestDatabeanWithManagedIndexByBazKey();
	}

	public TestDatabeanWithManagedIndexByBaz(String baz, String foo){
		this.key = new TestDatabeanWithManagedIndexByBazKey(baz);
		this.foo = foo;
	}

	@Override
	public Class<TestDatabeanWithManagedIndexByBazKey> getKeyClass(){
		return TestDatabeanWithManagedIndexByBazKey.class;
	}

	@Override
	public TestDatabeanWithManagedIndexByBazKey getKey(){
		return key;
	}

	@Override
	public TestDatabeanKey getTargetKey(){
		return new TestDatabeanKey(foo);
	}

	@Override
	public List<TestDatabeanWithManagedIndexByBaz> createFromDatabean(TestDatabean target){
		return Collections.singletonList(new TestDatabeanWithManagedIndexByBaz(target.getBaz(),
				target.getFoo()));
	}

	public String getA(){
		return foo;
	}

	public String getC(){
		return key.getC();
	}

}
