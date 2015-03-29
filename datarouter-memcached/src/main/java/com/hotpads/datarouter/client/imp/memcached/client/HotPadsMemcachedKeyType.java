package com.hotpads.datarouter.client.imp.memcached.client;

import com.hotpads.util.core.enums.HpEnum;

//TODO refactor this out of datarouter-memcached module
public enum HotPadsMemcachedKeyType implements HpEnum{
	HIB(0,"hib","Hibernate"), 
	HIB_QUERY(2, "hibQuery", "Hibernate Query"), //HIB_QUERY and HIB_TIMESTAMP are for hibernate query cacheing
	HIB_TIMESTAMP(3, "hibTimestamp", "Hibernate Timestamp"),
	RATE_LIMITER(8, "rate_limiter", "Rate limiter"),
	USER_LISTING_RECORDS_BY_TYPE(9, "userListingRecordsByType", "User Listing Records by Type"), 
	USER_SEARCH(10, "userSearch", "User Search"),
	ROBOT_DETECTOR(12, "robotDetector", "Robot Detector"),
	EXCEPTIONAL_IP(13,"exceptionalIP","Exceptional IP"),
	;
	
	private Integer value;
	private String key;
	private String display;
	private HotPadsMemcachedKeyType(Integer value,String key,String display){
		this.value=value;
		this.key=key;
		this.display=display;
	}
	public Integer getInteger(){ return value; }
	public String getDisplay(){ return display; }
	public String getKey(){ return key; }
	
	public static HotPadsMemcachedKeyType fromString(String val) {
		if (val == null)
			return HIB; //default
		
		for(HotPadsMemcachedKeyType type  : values()){
			if(type.getKey().equalsIgnoreCase(val)) return type;
		}
		
		return HIB; //default
	}
	
}