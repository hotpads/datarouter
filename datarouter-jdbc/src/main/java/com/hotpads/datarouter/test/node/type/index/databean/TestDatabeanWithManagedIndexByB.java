package com.hotpads.datarouter.test.node.type.index.databean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;

public class TestDatabeanWithManagedIndexByB extends
		BaseDatabean<TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB>
		implements
		UniqueIndexEntry<TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB, TestDatabeanKey, TestDatabean>{

	private TestDatabeanWithManagedIndexByBKey key;
	private String a;
	
	public static class TestDatabeanWithManagedIndexByBFielder extends BaseDatabeanFielder<TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB>{

		@Override
		public Class<? extends Fielder<TestDatabeanWithManagedIndexByBKey>> getKeyFielderClass(){
			return TestDatabeanWithManagedIndexByBKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndexByB databean){
			return Arrays.asList(new StringField("a", databean.a, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
		
	}
	
	public TestDatabeanWithManagedIndexByB(){
		this.key = new TestDatabeanWithManagedIndexByBKey();
	}
	
	public TestDatabeanWithManagedIndexByB(String b, String a){
		this.key = new TestDatabeanWithManagedIndexByBKey(b);
		this.a = a;
	}

	@Override
	public Class<TestDatabeanWithManagedIndexByBKey> getKeyClass(){
		return TestDatabeanWithManagedIndexByBKey.class;
	}

	@Override
	public TestDatabeanWithManagedIndexByBKey getKey(){
		return key;
	}

	@Override
	public TestDatabeanKey getTargetKey(){
		return new TestDatabeanKey(a);
	}

	@Override
	public List<TestDatabeanWithManagedIndexByB> createFromDatabean(TestDatabean target){
		TestDatabeanWithManagedIndexByB entry = new TestDatabeanWithManagedIndexByB(target.getB(), target.getA());
		return Collections.singletonList(entry);
	}
	
	public String getA(){
		return a;
	}
	
	public String getB(){
		return key.getB();
	}

}
