package com.hotpads.handler.user.authenticate.api;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;

public class ApiRequest extends BaseDatabean<ApiRequestKey, ApiRequest>{

	/** fields ********************************************************************************************************/
	
	private ApiRequestKey key;
	private Date requestDate;

	public static class F {
		public static final String
			requestDate = "requestDate";
	}
	
	/** fielder *******************************************************************************************************/
	
	public static class ApiRequestFielder 
	extends BaseDatabeanFielder<ApiRequestKey, ApiRequest> {

		@Override
		public Class<ApiRequestKey> getKeyFielderClass(){
			return ApiRequestKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(ApiRequest d){
			return FieldTool.createList(
				new DateField(F.requestDate, d.requestDate));					
		}	
		
		@Override
		public MySqlCharacterSet getCharacterSet(ApiRequest databean){
			return MySqlCharacterSet.latin1;
		}
		@Override
		public MySqlCollation getCollation(ApiRequest databean){
			return MySqlCollation.latin1_swedish_ci;
		}
	}
	
	/** constructors **************************************************************************************************/
	
	ApiRequest() {
		this.key = new ApiRequestKey();
	}
	
	public ApiRequest(String apiKey, String nonce, String signature, String timestamp) {
		this.key = new ApiRequestKey(apiKey, nonce, signature, timestamp);
	}
	
	/** databean ******************************************************************************************************/
	
	@Override
	public Class<ApiRequestKey> getKeyClass(){
		return ApiRequestKey.class;
	}

	@Override
	public ApiRequestKey getKey(){
		return key;
	}

	/** getters/setters ***********************************************************************************************/
	
	public void setKey(ApiRequestKey key) {
		this.key = key;
	}
	
	public Date getRequestDate() {
		return requestDate;
	}
	
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	
	public String getApiKey() {
		return key.getApiKey();
	}
	
	public void setApiKey(String apiKey) {
		this.key.setApiKey(apiKey);
	}
	
	public String getNonce() {
		return key.getNonce();
	}
	
	public void setNonce(String nonce) {
		this.key.setNonce(nonce);
	}
	
	public String getSignature() {
		return key.getSignature();
	}
	
	public void setSignature(String signature) {
		this.key.setSignature(signature);
	}
	
	public String getTimestamp() {
		return key.getTimestamp();
	}
	
	public void setTimestamp(String timestamp) {
		this.key.setTimestamp(timestamp);
	}
	
}
