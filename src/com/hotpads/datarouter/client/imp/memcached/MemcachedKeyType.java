package com.hotpads.datarouter.client.imp.memcached;

import com.hotpads.util.core.enums.HpEnum;

public enum MemcachedKeyType implements HpEnum{
	HIB(0,"hib","Hibernate"), 
	GEO(1,"geo","Geocoder"),
	HIB_QUERY(2, "hibQuery", "Hibernate Query"), //HIB_QUERY and HIB_TIMESTAMP are for hibernate query cacheing
	HIB_TIMESTAMP(3, "hibTimestamp", "Hibernate Timestamp"),
	SESSION(4, "session", "Session"),
	COORDINATES(5, "coordinates", "Area Coordinates"),
	USER_REAL_ESTATE_LISTING_IDS_BY_TYPE(6, "userRealEstateListingIdsByType", "User Real Estate Listing Ids by Type"),
	WIKIPEDIA(7, "wikipedia", "Wikipedia Articles"),
	RATE_LIMITER(8, "rate_limiter", "Rate limiter"),
	USER_LISTING_RECORDS_BY_TYPE(9, "userListingRecordsByType", "User Listing Records by Type"), 
	USER_SEARCH(10, "userSearch", "User Search"),
	EXPERIMENT(11, "experiment", "Experiment"),
	ROBOT_DETECTOR(12, "robotDetector", "Robot Detector"),
	EXCEPTIONAL_IP(13,"exceptionalIP","Exceptional IP"),
	;
	
	private Integer value;
	private String key;
	private String display;
	private MemcachedKeyType(Integer value,String key,String display){
		this.value=value;
		this.key=key;
		this.display=display;
	}
	public Integer getInteger(){ return value; }
	public String getDisplay(){ return display; }
	public String getKey(){ return key; }
	
	public static MemcachedKeyType fromString(String val) {
		if (val == null)
			return HIB; //default
		
		for(MemcachedKeyType type  : values()){
			if(type.getKey().equalsIgnoreCase(val)) return type;
		}
		
		return HIB; //default
	}
	
}