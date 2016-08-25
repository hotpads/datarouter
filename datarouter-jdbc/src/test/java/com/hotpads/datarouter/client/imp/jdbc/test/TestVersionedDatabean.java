package com.hotpads.datarouter.client.imp.jdbc.test;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseVersionedDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseVersionedDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.test.TestDatabeanKey;

public class TestVersionedDatabean extends BaseVersionedDatabean<TestDatabeanKey,TestVersionedDatabean>{

	private TestDatabeanKey key;
	private String bar;

	private static class FieldKeys{
		private static final StringFieldKey bar = new StringFieldKey("bar");
	}

	public static class TestVersionedDatabeanFielder
	extends BaseVersionedDatabeanFielder<TestDatabeanKey,TestVersionedDatabean>{

		public TestVersionedDatabeanFielder(){
			super(TestDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getVersionedNonKeyFields(TestVersionedDatabean databean){
			return Arrays.asList(new StringField(FieldKeys.bar, databean.bar));
		}

	}

	public TestVersionedDatabean(){
		this(new TestDatabeanKey(), null);
	}

	public TestVersionedDatabean(TestDatabeanKey key, String bar){
		this.key = key;
		this.bar = bar;
	}

	@Override
	public Class<TestDatabeanKey> getKeyClass(){
		return TestDatabeanKey.class;
	}

	@Override
	public TestDatabeanKey getKey(){
		return key;
	}

	public String getBar(){
		return bar;
	}

	public void setBar(String bar){
		this.bar = bar;
	}

}
