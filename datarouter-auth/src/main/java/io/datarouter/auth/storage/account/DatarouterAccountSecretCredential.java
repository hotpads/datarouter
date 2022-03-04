/*
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

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.util.PasswordTool;

public class DatarouterAccountSecretCredential
extends BaseDatabean<DatarouterAccountSecretCredentialKey,DatarouterAccountSecretCredential>{

	private String secretNamespace;
	private String accountName;
	private Date created;
	private String creatorUsername;
	private Date lastUsed;
	private Boolean active;

	public static class FieldKeys{
		private static final StringFieldKey secretNamespace = new StringFieldKey("secretNamespace");
		private static final StringFieldKey accountName = new StringFieldKey("accountName");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey created = new DateFieldKey("created");
		private static final StringFieldKey creatorUsername = new StringFieldKey("creatorUsername");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey lastUsed = new DateFieldKey("lastUsed");
		private static final BooleanFieldKey active = new BooleanFieldKey("active");
	}

	public DatarouterAccountSecretCredential(){
		super(new DatarouterAccountSecretCredentialKey());
	}

	public DatarouterAccountSecretCredential(String secretName, String secretNamespace, String accountName,
			String creatorUsername){
		super(new DatarouterAccountSecretCredentialKey(secretName));
		this.secretNamespace = secretNamespace;
		this.accountName = accountName;
		this.created = new Date();
		this.creatorUsername = creatorUsername;
		this.active = true;
	}

	public static DatarouterAccountSecretCredential create(String secretNamespace, String accountName,
			String creatorUsername){
		return new DatarouterAccountSecretCredential(PasswordTool.generateSalt(), secretNamespace, accountName,
				creatorUsername);
	}

	public static class DatarouterAccountSecretCredentialFielder
	extends BaseDatabeanFielder<DatarouterAccountSecretCredentialKey,DatarouterAccountSecretCredential>{

		public DatarouterAccountSecretCredentialFielder(){
			super(DatarouterAccountSecretCredentialKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccountSecretCredential credential){
			return List.of(
					new StringField(FieldKeys.secretNamespace, credential.getSecretNamespace()),
					new StringField(FieldKeys.accountName, credential.accountName),
					new DateField(FieldKeys.created, credential.created),
					new StringField(FieldKeys.creatorUsername, credential.creatorUsername),
					new DateField(FieldKeys.lastUsed, credential.lastUsed),
					new BooleanField(FieldKeys.active, credential.active));
		}

	}

	@Override
	public Supplier<DatarouterAccountSecretCredentialKey> getKeySupplier(){
		return DatarouterAccountSecretCredentialKey::new;
	}

	public String getAccountName(){
		return accountName;
	}

	public void setAccountName(String accountName){
		this.accountName = accountName;
	}

	public String getSecretNamespace(){
		return secretNamespace;
	}

	public String getCreatedDate(ZoneId zoneId){
		return ZonedDateFormatterTool.formatInstantWithZone(getCreatedInstant(), zoneId);
	}

	public Instant getCreatedInstant(){
		if(created == null){
			return Instant.EPOCH;
		}
		return created.toInstant();
	}

	public String getCreatorUsername(){
		return creatorUsername;
	}

	public void setLastUsed(Date lastUsed){
		this.lastUsed = lastUsed;
	}

	public String getLastUsedDate(ZoneId zoneId){
		return ZonedDateFormatterTool.formatInstantWithZone(getLastUsedInstant(), zoneId);
	}

	public Instant getLastUsedInstant(){
		if(lastUsed == null){
			return Instant.EPOCH;
		}
		return lastUsed.toInstant();
	}

	public Boolean getActive(){
		return active == null || active;
	}

	public void setActive(Boolean active){
		this.active = active;
	}

}
