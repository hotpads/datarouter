package com.hotpads.datarouter.test;

import com.hotpads.datarouter.storage.databean.BaseDatabean;

public class TestDatabean extends BaseDatabean<TestDatabeanKey, TestDatabean>{

	private TestDatabeanKey key;
	private String b;
	private String c;

	public TestDatabean(){
		this.key = new TestDatabeanKey();
	}
	
	public TestDatabean(String a, String b, String c){
		this.key = new TestDatabeanKey(a);
		this.b = b;
		this.c = c;
	}
	
	@Override
	public Class<TestDatabeanKey> getKeyClass(){
		return TestDatabeanKey.class;
	}

	@Override
	public TestDatabeanKey getKey(){
		return key;
	}

	public String getB(){
		return b;
	}

	public String getC(){
		return c;
	}

	public String getA(){
		return key.getA();
	}

}
