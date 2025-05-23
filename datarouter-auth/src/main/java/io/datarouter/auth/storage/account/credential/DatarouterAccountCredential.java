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
package io.datarouter.auth.storage.account.credential;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.util.PasswordTool;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;
import io.datarouter.util.time.ZonedDateFormatterTool;

public class DatarouterAccountCredential
extends BaseDatabean<DatarouterAccountCredentialKey,DatarouterAccountCredential>{

	private String accountName;
	private String secretKey;
	private MilliTime createdAt;
	private String creatorUsername;
	private MilliTime lastUsedAt;
	private Boolean active;

	public static class FieldKeys{
		public static final StringFieldKey accountName = new StringFieldKey("accountName");
		private static final StringFieldKey secretKey = new StringFieldKey("secretKey");
		private static final StringFieldKey creatorUsername = new StringFieldKey("creatorUsername");
		private static final BooleanFieldKey active = new BooleanFieldKey("active");
	}

	public DatarouterAccountCredential(){
		super(new DatarouterAccountCredentialKey());
	}

	public DatarouterAccountCredential(String apiKey, String secretKey, String accountName, String creatorUsername){
		super(new DatarouterAccountCredentialKey(apiKey));
		this.secretKey = secretKey;
		this.accountName = accountName;
		this.createdAt = MilliTime.now();
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
			super(DatarouterAccountCredentialKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccountCredential credential){
			return List.of(
					new StringField(FieldKeys.accountName, credential.accountName),
					new StringField(FieldKeys.secretKey, credential.secretKey),
					new LongEncodedField<>(DatarouterAccount.FieldKeys.createdAt, credential.createdAt),
					new StringField(FieldKeys.creatorUsername, credential.creatorUsername),
					new LongEncodedField<>(DatarouterAccount.FieldKeys.lastUsedAt, credential.lastUsedAt),
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

	public void setAccountName(String accountName){
		this.accountName = accountName;
	}

	public String getSecretKey(){
		return secretKey;
	}

	public String getCreatedDate(ZoneId zoneId){
		return ZonedDateFormatterTool.formatInstantWithZone(getCreatedInstant(), zoneId);
	}

	public Instant getCreatedInstant(){
		if(createdAt == null){
			return Instant.EPOCH;
		}
		return createdAt.toInstant();
	}

	public String getCreatorUsername(){
		return creatorUsername;
	}

	public void setLastUsed(MilliTime lastUsedAt){
		this.lastUsedAt = lastUsedAt;
	}

	public String getLastUsedDate(ZoneId zoneId){
		return ZonedDateFormatterTool.formatInstantWithZone(getLastUsedInstant(), zoneId);
	}

	public MilliTime getLastUsed(){
		return lastUsedAt;
	}

	public Instant getLastUsedInstant(){
		if(lastUsedAt == null){
			return Instant.EPOCH;
		}
		return lastUsedAt.toInstant();
	}

	public Boolean getActive(){
		return active == null || active;
	}

	public void setActive(Boolean active){
		this.active = active;
	}

}
