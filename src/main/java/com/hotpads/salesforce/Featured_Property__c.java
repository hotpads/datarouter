package com.hotpads.salesforce;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.TreeSet;

import com.google.gson.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseField.FieldColumnNameComparator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.salesforce.databean.SalesforceDatabean;
import com.hotpads.salesforce.databean.SalesforceDatabeanKey;
import com.hotpads.salesforce.dto.SalesforceQueryResult;

//Variable names and Databean name must be the same as Salesforce's
public class Featured_Property__c extends SalesforceDatabean{
	
	private String Name;
	private String Property_Zillow_Account_ID__c;//Blame Salesforce for this wonderful field name
	private Date Onboarding_Featured_Date__c;
	private Date Manual_Delisting_Date__c;
	private Date CreatedDate;
		
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
	
	@Override
	public TreeSet<Field<?>> getFields(){
		TreeSet<Field<?>> fields = new TreeSet<>(new FieldColumnNameComparator());
		fields.add(new DateField("Onboarding_Featured_Date__c", Onboarding_Featured_Date__c));
		fields.add(new DateField("Manual_Delisting_Date__c", Manual_Delisting_Date__c));
		fields.add(new StringField("Name", Name, 0));
		fields.add(new StringField("Property_Zillow_Account_ID__c", Property_Zillow_Account_ID__c, 0));
		return fields;
	}

	public Date getOnboarding_Featured_Date__c(){
		return Onboarding_Featured_Date__c;
	}

	public void setOnboarding_Featured_Date__c(Date onboarding_Featured_Date__c){
		Onboarding_Featured_Date__c = onboarding_Featured_Date__c;
	}

	public Date getManual_Delisting_Date__c(){
		return Manual_Delisting_Date__c;
	}

	public void setManual_Delisting_Date__c(Date manual_Delisting_Date__c){
		Manual_Delisting_Date__c = manual_Delisting_Date__c;
	}

	public Date getCreatedDate(){
		return CreatedDate;
	}

	public void setCreatedDate(Date createdDate){
		CreatedDate = createdDate;
	}
	
}