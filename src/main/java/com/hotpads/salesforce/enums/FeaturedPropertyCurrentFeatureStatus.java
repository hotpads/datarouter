package com.hotpads.salesforce.enums;

public enum FeaturedPropertyCurrentFeatureStatus{

	PENDING("Pending"),
	FEATURED("Featured"),
	EXPIRED("Expired");
	
	String displayName;
	
	FeaturedPropertyCurrentFeatureStatus(String displayName){
		this.displayName = displayName;
	}

	public String getDisplayName(){
		return displayName;
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}
	
}
