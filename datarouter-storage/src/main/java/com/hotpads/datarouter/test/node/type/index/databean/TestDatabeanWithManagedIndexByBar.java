package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;

public class TestDatabeanWithManagedIndexByBar
extends BaseDatabean<TestDatabeanWithManagedIndexByBarKey, TestDatabeanWithManagedIndexByBar>
implements UniqueIndexEntry<
		TestDatabeanWithManagedIndexByBarKey,
		TestDatabeanWithManagedIndexByBar,
		TestDatabeanKey,
		TestDatabean>{

	private TestDatabeanWithManagedIndexByBarKey key;
	private String foo;

	public static class TestDatabeanWithManagedIndexByBFielder
	extends BaseDatabeanFielder<TestDatabeanWithManagedIndexByBarKey,TestDatabeanWithManagedIndexByBar>{

		public TestDatabeanWithManagedIndexByBFielder(){
			super(TestDatabeanWithManagedIndexByBarKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndexByBar databean){
			return Arrays.asList(new StringField("foo", databean.foo, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	}

	public TestDatabeanWithManagedIndexByBar(){
		this.key = new TestDatabeanWithManagedIndexByBarKey();
	}

	public TestDatabeanWithManagedIndexByBar(String bar, String foo){
		this.key = new TestDatabeanWithManagedIndexByBarKey(bar);
		this.foo = foo;
	}

	@Override
	public Class<TestDatabeanWithManagedIndexByBarKey> getKeyClass(){
		return TestDatabeanWithManagedIndexByBarKey.class;
	}

	@Override
	public TestDatabeanWithManagedIndexByBarKey getKey(){
		return key;
	}

	@Override
	public TestDatabeanKey getTargetKey(){
		return new TestDatabeanKey(foo);
	}

	@Override
	public List<TestDatabeanWithManagedIndexByBar> createFromDatabean(TestDatabean target){
		TestDatabeanWithManagedIndexByBar entry = new TestDatabeanWithManagedIndexByBar(target.getBar(),
				target.getFoo());
		return Collections.singletonList(entry);
	}

	public String getFoo(){
		return foo;
	}

	public String getBar(){
		return key.getBar();
	}

}
