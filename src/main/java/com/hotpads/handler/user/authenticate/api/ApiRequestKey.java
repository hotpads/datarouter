package com.hotpads.handler.user.authenticate.api;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial") 
public class ApiRequestKey extends BasePrimaryKey<ApiRequestKey> {

	public static final int	DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/** fields ********************************************************************************************************/										
	
	private String apiKey;
	private String nonce;
	private String signature;
	private String timestamp;
	
	/** columns *******************************************************************************************************/

	public static class F {
		public static final String
			apiKey = "apiKey",
			nonce = "nonce",
			signature = "signature",
			timestamp = "timestamp";
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(F.apiKey, apiKey, DEFAULT_STRING_LENGTH),
				new StringField(F.nonce, nonce, DEFAULT_STRING_LENGTH),
				new StringField(F.signature, signature, DEFAULT_STRING_LENGTH),
				new StringField(F.timestamp, timestamp, DEFAULT_STRING_LENGTH));				
	}
	
	/** constructors **************************************************************************************************/
	ApiRequestKey() {}
	
	public ApiRequestKey(String apiKey, String nonce, String signature, String timestamp) {
		this.apiKey = apiKey;
		this.nonce = nonce;
		this.signature = signature;
		this.timestamp = timestamp;
	}
	
	/** getters/setters ***********************************************************************************************/
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getNonce() {
		return nonce;
	}
	
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
}
