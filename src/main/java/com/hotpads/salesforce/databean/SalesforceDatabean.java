package com.hotpads.salesforce.databean;

import java.lang.reflect.Type;

import com.hotpads.salesforce.dto.Attributes;

public abstract class SalesforceDatabean{ //TODO extends BaseDatabean
	
	private transient SalesforceDatabeanKey key;
	private Attributes attributes;
	
	//TODO add fielder
	
	public SalesforceDatabean(SalesforceDatabeanKey key){
		this.key = key;
	}
	
	public SalesforceDatabeanKey getKey(){
		return key;
	}
	
	public void setKey(SalesforceDatabeanKey key){
		this.key = key;
	}
	
	public abstract Type getQueryResultType();

	public Attributes getAttributes(){
		return attributes;
	}

	public void setAttributes(Attributes attributes){
		this.attributes = attributes;
	}
	
}