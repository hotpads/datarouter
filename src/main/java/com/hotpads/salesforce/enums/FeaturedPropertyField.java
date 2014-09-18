package com.hotpads.salesforce.enums;

import java.util.Arrays;
import java.util.List;


public enum FeaturedPropertyField{
	PROPERTY_ZILLOW_ACCOUNT_ID("Property_Zillow_Account_ID__c",false),
	ONBOARDING_FEATURED_DATE("Onboarding_Featured_Date__c",true),
	MANUAL_DELISTING_DATE("Manual_Delisting_Date__c",true),
	CURRENT_FEATURE_STATUS("Current_Feature_Status__c",false);
	
	
	public static final List<String> DEFAULT_AUTHORIZED_FIELD_NAME_LIST =
			Arrays.asList(FeaturedPropertyField.ONBOARDING_FEATURED_DATE.getFieldName(),
					FeaturedPropertyField.MANUAL_DELISTING_DATE.getFieldName());
	
	public static final List<String> ONBOARDING_FEATURED_DATE_FIELD_NAME_LIST =
			Arrays.asList(FeaturedPropertyField.ONBOARDING_FEATURED_DATE.getFieldName());
	
	private String fieldName;
	private boolean writeable =false;
	
	FeaturedPropertyField(String fieldName, boolean writeable){
		this.fieldName = fieldName;
		this.writeable = writeable;
	}

	public String getFieldName(){
		return fieldName;
	}

	public void setFieldName(String fieldName){
		this.fieldName = fieldName;
	}

	public boolean isWriteable(){
		return writeable;
	}

	public void setWriteable(boolean writeable){
		this.writeable = writeable;
	}

}

