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
import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class ApiRequest extends BaseDatabean<ApiRequestKey,ApiRequest>{

	/** fields ********************************************************************************************************/

	private ApiRequestKey key;
	private Date requestDate;

	public static class FieldKeys{
		public static final DateFieldKey requestDate = new DateFieldKey("requestDate");
	}

	/** fielder *******************************************************************************************************/

	public static class ApiRequestFielder
	extends BaseDatabeanFielder<ApiRequestKey,ApiRequest>{

		public ApiRequestFielder(){
			super(ApiRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ApiRequest request){
			return Arrays.asList(
					new DateField(FieldKeys.requestDate, request.requestDate));
		}
	}

	/** constructors **************************************************************************************************/

	public ApiRequest(){
		this.key = new ApiRequestKey();
	}

	public ApiRequest(String apiKey, String nonce, String signature, String timestamp){
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

	public void setKey(ApiRequestKey key){
		this.key = key;
	}

	public Date getRequestDate(){
		return requestDate;
	}

	public void setRequestDate(Date requestDate){
		this.requestDate = requestDate;
	}

	public String getApiKey(){
		return key.getApiKey();
	}

	public void setApiKey(String apiKey){
		this.key.setApiKey(apiKey);
	}

	public String getNonce(){
		return key.getNonce();
	}

	public void setNonce(String nonce){
		this.key.setNonce(nonce);
	}

	public String getSignature(){
		return key.getSignature();
	}

	public void setSignature(String signature){
		this.key.setSignature(signature);
	}

	public String getTimestamp(){
		return key.getTimestamp();
	}

	public void setTimestamp(String timestamp){
		this.key.setTimestamp(timestamp);
	}

}
