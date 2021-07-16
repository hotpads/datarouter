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
import io.datarouter.util.time.ZonedDateFormaterTool;
import io.datarouter.web.util.PasswordTool;

public class DatarouterAccountCredential
extends BaseDatabean<DatarouterAccountCredentialKey,DatarouterAccountCredential>{

	private String accountName;
	private String secretKey;
	private Date created;
	private String creatorUsername;
	private Date lastUsed;
	private Boolean active;

	public static class FieldKeys{
		public static final StringFieldKey accountName = new StringFieldKey("accountName");
		private static final StringFieldKey secretKey = new StringFieldKey("secretKey");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey created = new DateFieldKey("created");
		private static final StringFieldKey creatorUsername = new StringFieldKey("creatorUsername");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey lastUsed = new DateFieldKey("lastUsed");
		private static final BooleanFieldKey active = new BooleanFieldKey("active");
	}

	public DatarouterAccountCredential(){
		super(new DatarouterAccountCredentialKey());
	}

	public DatarouterAccountCredential(String apiKey, String secretKey, String accountName, String creatorUsername){
		super(new DatarouterAccountCredentialKey(apiKey));
		this.secretKey = secretKey;
		this.accountName = accountName;
		this.created = new Date();
		this.creatorUsername = creatorUsername;
		this.active = true;
	}

	public static DatarouterAccountCredential create(String accountName, String creatorUsername){
		return new DatarouterAccountCredential(PasswordTool.generateSalt(), PasswordTool.generateSalt(), accountName,
				creatorUsername);
	}

	public static class DatarouterAccountCredentialFielder
	extends BaseDatabeanFielder<DatarouterAccountCredentialKey,DatarouterAccountCredential>{

		public DatarouterAccountCredentialFielder(){
			super(DatarouterAccountCredentialKey.class);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccountCredential credential){
			return List.of(
					new StringField(FieldKeys.accountName, credential.accountName),
					new StringField(FieldKeys.secretKey, credential.secretKey),
					new DateField(FieldKeys.created, credential.created),
					new StringField(FieldKeys.creatorUsername, credential.creatorUsername),
					new DateField(FieldKeys.lastUsed, credential.lastUsed),
					new BooleanField(FieldKeys.active, credential.active));
		}

	}

	@Override
	public Supplier<DatarouterAccountCredentialKey> getKeySupplier(){
		return DatarouterAccountCredentialKey::new;
	}

	public String getAccountName(){
		return accountName;
	}

	public String getSecretKey(){
		return secretKey;
	}

	public String getCreatedDate(ZoneId zoneId){
		return ZonedDateFormaterTool.formatInstantWithZone(getCreatedInstant(), zoneId);
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
		return ZonedDateFormaterTool.formatInstantWithZone(getLastUsedInstant(), zoneId);
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
