package com.hotpads.datarouter.node.type.index.databean;

import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public class TestDatabeanWithManagedIndexByC extends
		BaseDatabean<TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC>
		implements
		MultiIndexEntry<TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC, TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex>{

	private TestDatabeanWithManagedIndexByCKey key;
	private String a;
	
	public static class TestDatabeanWithManagedIndexByCFielder extends BaseDatabeanFielder<TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC>{

		@Override
		public Class<? extends Fielder<TestDatabeanWithManagedIndexByCKey>> getKeyFielderClass(){
			return TestDatabeanWithManagedIndexByCKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndexByC databean){
			return FieldTool.createList(new StringField("a", databean.a, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
		
	}
	
	public TestDatabeanWithManagedIndexByC(){
		this.key = new TestDatabeanWithManagedIndexByCKey();
	}
	
	public TestDatabeanWithManagedIndexByC(String c, String a){
		this.key = new TestDatabeanWithManagedIndexByCKey(c);
		this.a = a;
	}

	@Override
	public Class<TestDatabeanWithManagedIndexByCKey> getKeyClass(){
		return TestDatabeanWithManagedIndexByCKey.class;
	}

	@Override
	public TestDatabeanWithManagedIndexByCKey getKey(){
		return key;
	}

	@Override
	public TestDatabeanWithManagedIndexKey getTargetKey(){
		return new TestDatabeanWithManagedIndexKey(a);
	}

	@Override
	public List<TestDatabeanWithManagedIndexByC> createFromDatabean(TestDatabeanWithManagedIndex target){
		TestDatabeanWithManagedIndexByC entry = new TestDatabeanWithManagedIndexByC(target.getC(), target.getA());
		return Collections.singletonList(entry);
	}
	
	public String getA(){
		return a;
	}
	
	public String getC(){
		return key.getC();
	}

}
