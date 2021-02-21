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
package io.datarouter.auth.storage.account;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.util.DateTool;
import io.datarouter.web.util.PasswordTool;

public class DatarouterAccount extends BaseDatabean<DatarouterAccountKey,DatarouterAccount>{

	private String apiKey;
	private String secretKey;
	private Date created;
	private String creator;
	private Date lastUsed;
	private Boolean enableUserMappings;

	private static class FieldKeys{
		private static final StringFieldKey apiKey = new StringFieldKey("apiKey");
		private static final StringFieldKey secretKey = new StringFieldKey("secretKey");
		private static final DateFieldKey created = new DateFieldKey("created");
		private static final StringFieldKey creator = new StringFieldKey("creator");
		private static final DateFieldKey lastUsed = new DateFieldKey("lastUsed");
		private static final BooleanFieldKey enableUserMappings = new BooleanFieldKey("enableUserMappings");
	}

	public DatarouterAccount(){
		super(new DatarouterAccountKey());
	}

	public DatarouterAccount(String accountName, Date created, String creator){
		super(new DatarouterAccountKey(accountName));
		resetApiKey();
		resetSecretKey();
		this.created = created;
		this.creator = creator;
		this.enableUserMappings = false;
	}

	public static class DatarouterAccountFielder extends BaseDatabeanFielder<DatarouterAccountKey,DatarouterAccount>{

		public DatarouterAccountFielder(){
			super(DatarouterAccountKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccount account){
			return List.of(
					new StringField(FieldKeys.apiKey, account.apiKey),
					new StringField(FieldKeys.secretKey, account.secretKey),
					new DateField(FieldKeys.created, account.created),
					new StringField(FieldKeys.creator, account.creator),
					new DateField(FieldKeys.lastUsed, account.lastUsed),
					new BooleanField(FieldKeys.enableUserMappings, account.enableUserMappings));
		}

	}

	@Override
	public Class<DatarouterAccountKey> getKeyClass(){
		return DatarouterAccountKey.class;
	}

	public String getApiKey(){
		return apiKey;
	}

	public String getSecretKey(){
		return secretKey;
	}

	public void resetApiKeyToDefault(String defaultApiKey){
		apiKey = defaultApiKey;
	}

	public void resetSecretKeyToDefault(String defaultSecretKey){
		secretKey = defaultSecretKey;
	}

	public void resetApiKey(){
		apiKey = PasswordTool.generateSalt();
	}

	public void resetSecretKey(){
		secretKey = PasswordTool.generateSalt();
	}

	public void setLastUsed(Date lastUsed){
		this.lastUsed = lastUsed;
	}

	public String getLastUsedDate(ZoneId zoneId){
		if(lastUsed == null){
			return "";
		}
		return DateTool.formatDateWithZone(lastUsed, zoneId);
	}

	public void toggleUserMappings(){
		if(enableUserMappings == null){
			enableUserMappings = true;
		}else{
			enableUserMappings = !enableUserMappings;
		}
	}

	public boolean getEnableUserMappings(){
		if(enableUserMappings == null){
			return false;
		}
		return enableUserMappings;
	}

	public void setEnableUserMappings(boolean enableUserMappings){
		this.enableUserMappings = enableUserMappings;
	}

}
