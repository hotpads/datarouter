package com.hotpads.datarouter.client.imp.kinesis.client;

import com.hotpads.util.core.enums.StringPersistedEnum;

public enum AmazonRegion implements StringPersistedEnum{
	GovCloud("us-gov-west-1"),
    US_EAST_1("us-east-1"),
    US_WEST_1("us-west-1"),
    US_WEST_2("us-west-2"),
    EU_WEST_1("eu-west-1"),
    EU_CENTRAL_1("eu-central-1"),
    AP_SOUTH_1("ap-south-1"),
    AP_SOUTHEAST_1("ap-southeast-1"),
    AP_SOUTHEAST_2("ap-southeast-2"),
    AP_NORTHEAST_1("ap-northeast-1"),
    AP_NORTHEAST_2("ap-northeast-2"),
    SA_EAST_1("sa-east-1"),
    CN_NORTH_1("cn-north-1");

	private String regionName;

	private AmazonRegion(String regionName){
		this.regionName = regionName;
	}

	@Override
	public String getPersistentString(){
		return regionName;
	}

	@Override
	public String getDisplay(){
		return regionName;
	}

}
