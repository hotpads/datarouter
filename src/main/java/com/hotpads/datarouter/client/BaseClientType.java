package com.hotpads.datarouter.client;

import com.hotpads.util.core.ClassTool;

public abstract class BaseClientType
implements Comparable<ClientType>{

	public BaseClientType(){
	}

	@Override
	public int compareTo(ClientType that){
		return ClassTool.compareClass(this, that);
	}
	
	@Override
	public boolean equals(Object that){
		return ClassTool.sameClass(this, that);
	}
	
	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
	
}
