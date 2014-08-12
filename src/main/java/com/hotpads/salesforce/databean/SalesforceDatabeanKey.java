package com.hotpads.salesforce.databean;

public class SalesforceDatabeanKey{ //TODO extends BaseDatabeanKey
	private String id;

	public SalesforceDatabeanKey(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

}