package com.hotpads.salesforce;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.hotpads.salesforce.databean.SalesforceDatabean;
import com.hotpads.salesforce.databean.SalesforceDatabeanKey;
import com.hotpads.salesforce.dto.SalesforceQueryResult;

//Variable names and Databean name must be the same as Salesforce's
public class Featured_Property__c extends SalesforceDatabean{
	
	private String Name;
	private String Property_Zillow_Account_ID__c;//Blame Salesforce for this wonderful field name

	public Featured_Property__c(){
		super(new SalesforceDatabeanKey(""));
	}
	
	public Featured_Property__c(SalesforceDatabeanKey key){
		super(key);
	}
	
	public String getProperty_Zillow_Account_ID__c(){
		return Property_Zillow_Account_ID__c;
	}

	@Override
	public Type getQueryResultType(){
		return new TypeToken<SalesforceQueryResult<Featured_Property__c>>(){}.getType();
	}

	public String getName(){
		return Name;
	}
	
}