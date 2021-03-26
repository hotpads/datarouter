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

public class DatarouterAccountCredential
extends BaseDatabean<DatarouterAccountCredentialKey,DatarouterAccountCredential>{

	private String accountName;
	private String secretKey;
	private Date created;
	private String creatorUsername;
	private Date lastUsed;
	private Boolean enableUserMappings;

	static class FieldKeys{
		static final StringFieldKey accountName = new StringFieldKey("accountName");
		private static final StringFieldKey secretKey = new StringFieldKey("secretKey");
		private static final DateFieldKey created = new DateFieldKey("created");
		private static final StringFieldKey creatorUsername = new StringFieldKey("creatorUsername");
		private static final DateFieldKey lastUsed = new DateFieldKey("lastUsed");
		private static final BooleanFieldKey enableUserMappings = new BooleanFieldKey("enableUserMappings");
	}

	public DatarouterAccountCredential(){
		super(new DatarouterAccountCredentialKey());
	}

	public DatarouterAccountCredential(String apiKey, String accountName, Date created, String creatorUsername){
		super(new DatarouterAccountCredentialKey(apiKey));
		this.accountName = accountName;
		this.secretKey = PasswordTool.generateSalt();
		this.created = created;
		this.creatorUsername = creatorUsername;
		this.enableUserMappings = false;
	}

	//temporary
	DatarouterAccountCredential(String apiKey, String accountName, String secretKey, Date created,
			String creatorUsername, Date lastUsed, Boolean enableUserMappings){
		super(new DatarouterAccountCredentialKey(apiKey));
		this.accountName = accountName;
		this.secretKey = secretKey;
		this.created = created;
		this.creatorUsername = creatorUsername;
		this.lastUsed = lastUsed;
		this.enableUserMappings = enableUserMappings;
	}

	public static class DatarouterAccountCredentialFielder
	extends BaseDatabeanFielder<DatarouterAccountCredentialKey,DatarouterAccountCredential>{

		public DatarouterAccountCredentialFielder(){
			super(DatarouterAccountCredentialKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccountCredential account){
			return List.of(
					new StringField(FieldKeys.accountName, account.accountName),
					new StringField(FieldKeys.secretKey, account.secretKey),
					new DateField(FieldKeys.created, account.created),
					new StringField(FieldKeys.creatorUsername, account.creatorUsername),
					new DateField(FieldKeys.lastUsed, account.lastUsed),
					new BooleanField(FieldKeys.enableUserMappings, account.enableUserMappings));
		}

	}

	@Override
	public Class<DatarouterAccountCredentialKey> getKeyClass(){
		return DatarouterAccountCredentialKey.class;
	}

	public String getAccountName(){
		return accountName;
	}

	public String getSecretKey(){
		return secretKey;
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

	//temporary methods

	Date getCreated(){
		return created;
	}

	String getCreatorUsername(){
		return creatorUsername;
	}

	Date getLastUsed(){
		return lastUsed;
	}

}
