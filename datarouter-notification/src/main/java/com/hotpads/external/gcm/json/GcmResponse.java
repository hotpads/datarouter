package com.hotpads.external.gcm.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GcmResponse{

	/*
	 * single recipient success case:
	 * {"multicast_id":6581123761680663065,"success":1,"failure":0,"canonical_ids":0,
	 *  "results":[{"message_id":"0:1441305322240464%cb304d02cb304d02"}]}
	 */

	@SerializedName("success")
	private Integer numSuccess;
	@SerializedName("failure")
	private Integer numFailures;

	private List<GcmResult> results;

	public Integer getNumFailures(){
		return numFailures;
	}

	public List<GcmResult> getResults(){
		return results;
	}

}
