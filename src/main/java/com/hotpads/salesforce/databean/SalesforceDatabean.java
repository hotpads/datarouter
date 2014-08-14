package com.hotpads.salesforce.databean;

import java.lang.reflect.Type;

import com.hotpads.salesforce.dto.SalesforceAttributes;

public abstract class SalesforceDatabean{ //TODO extends BaseDatabean
	
	private transient SalesforceDatabeanKey key;
	private SalesforceAttributes attributes;
	
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

	public SalesforceAttributes getAttributes(){
		return attributes;
	}

	public void setAttributes(SalesforceAttributes attributes){
		this.attributes = attributes;
	}
	
}