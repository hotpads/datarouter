package com.hotpads.handler.user.authenticate.api;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class ApiRequestKey extends BasePrimaryKey<ApiRequestKey> {

	public static final int	DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	/** fields ********************************************************************************************************/

	private String apiKey;
	private String nonce;
	private String signature;
	private String timestamp;

	public static class FieldKeys {
		public static final StringFieldKey apiKey = new StringFieldKey("apiKey");
		public static final StringFieldKey nonce = new StringFieldKey("nonce"),
		public static final StringFieldKey signature = new StringFieldKey("signature"),
		public static final StringFieldKey timestamp = new StringFieldKey("timestamp");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.apiKey, apiKey),
				new StringField(FieldKeys.nonce, nonce),
				new StringField(FieldKeys.signature, signature),
				new StringField(FieldKeys.timestamp, timestamp));
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
