/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.user.authenticate.api;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.BasePrimaryKey;
import io.datarouter.model.util.CommonFieldSizes;

public class ApiRequestKey extends BasePrimaryKey<ApiRequestKey>{

	public static final int DEFAULT_STRING_LENGTH = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	/** fields ********************************************************************************************************/

	private String apiKey;
	private String nonce;
	private String signature;
	private String timestamp;

	public static class FieldKeys{
		public static final StringFieldKey apiKey = new StringFieldKey("apiKey");
		public static final StringFieldKey nonce = new StringFieldKey("nonce");
		public static final StringFieldKey signature = new StringFieldKey("signature");
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

	public ApiRequestKey(String apiKey, String nonce, String signature, String timestamp){
		this.apiKey = apiKey;
		this.nonce = nonce;
		this.signature = signature;
		this.timestamp = timestamp;
	}

	/** getters/setters ***********************************************************************************************/

	public String getApiKey(){
		return apiKey;
	}

	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}

	public String getNonce(){
		return nonce;
	}

	public void setNonce(String nonce){
		this.nonce = nonce;
	}

	public String getSignature(){
		return signature;
	}

	public void setSignature(String signature){
		this.signature = signature;
	}

	public String getTimestamp(){
		return timestamp;
	}

	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}

}
