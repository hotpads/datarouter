package com.hotpads.external.gcm.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

public class GcmResult{
	private static final Logger logger = LoggerFactory.getLogger(GcmResult.class.getName());

	/*
	 * success:
	 * {"message_id":"0:1441305322240464%cb304d02cb304d02"}
	 *
	 * unknown error:
	 * {"error":"InternalServerError"}
	 *
	 * unregistered:
	 * {"error":"NotRegistered"}
	 */

	public static enum GcmResultError{
		INTERNAL_ERROR("InternalServerError"),
		NOT_REGISTERED("NotRegistered"),
		INVALID_REGISTRATION("InvalidRegistration"),
		;

		private final String name;

		GcmResultError(String name){
			this.name = name;
		}

		public static GcmResultError fromString(String input){
			for(GcmResultError error : values()){
				if(error.name.equals(input)){
					return error;
				}
			}
			logger.error("Unknown GCM error: " + input);
			return null;
		}
	}

	@SerializedName("message_id")
	private String messageId;
	private String error;


	public GcmResultError getGcmResultError(){
		return GcmResultError.fromString(error);
	}

	public String getError(){
		return error;
	}

}
