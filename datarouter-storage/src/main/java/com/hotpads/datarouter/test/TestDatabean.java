package com.hotpads.datarouter.test;

import com.hotpads.datarouter.storage.databean.BaseDatabean;

public class TestDatabean extends BaseDatabean<TestDatabeanKey, TestDatabean>{

	private TestDatabeanKey key;
	private String bar;
	private String baz;

	public TestDatabean(){
		this.key = new TestDatabeanKey();
	}

	public TestDatabean(String foo, String bar, String baz){
		this.key = new TestDatabeanKey(foo);
		this.bar = bar;
		this.baz = baz;
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

	public String getBaz(){
		return baz;
	}

	public String getFoo(){
		return key.getFoo();
	}

}
