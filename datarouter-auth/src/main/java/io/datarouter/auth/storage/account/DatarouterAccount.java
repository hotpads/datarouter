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
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;
import io.datarouter.util.string.StringTool;

public class DatarouterAccount extends BaseDatabean<DatarouterAccountKey,DatarouterAccount>{

	private MilliTime createdAt;
	private String creator;
	private MilliTime lastUsedAt;
	private Boolean enableUserMappings;
	public String callerType; // IN-9144
	/*
	 * The official spec spells this as "referer"
	 */
	public String referer;

	public static class FieldKeys{
		public static final LongEncodedFieldKey<MilliTime> createdAt = new LongEncodedFieldKey<>(
				"createdAt",
				new MilliTimeToLongFieldCodec());
		public static final StringFieldKey creator = new StringFieldKey("creator");
		public static final LongEncodedFieldKey<MilliTime> lastUsedAt = new LongEncodedFieldKey<>(
				"lastUsedAt",
				new MilliTimeToLongFieldCodec());
		public static final BooleanFieldKey enableUserMappings = new BooleanFieldKey("enableUserMappings");
		public static final StringFieldKey callerType = new StringFieldKey("callerType");
		public static final StringFieldKey referrer = new StringFieldKey("referrer");
		public static final StringFieldKey referer = new StringFieldKey("referer");
	}

	public DatarouterAccount(){
		super(new DatarouterAccountKey());
	}

	public DatarouterAccount(String accountName, MilliTime createdAt, String creator){
		super(new DatarouterAccountKey(accountName));
		this.createdAt = createdAt;
		this.creator = creator;
		this.enableUserMappings = false;
		this.callerType = null;
		this.referer = null;
	}

	/**
	 * This constructor is specifically used to migrating account names
	 *
	 * @param accountName new accountName
	 * @param account the old account databean
	 */
	public DatarouterAccount(
			String accountName,
			DatarouterAccount account){
		this(
				accountName,
				account.getCreated(),
				account.getCreator());
		this.enableUserMappings = account.getEnableUserMappings();
		this.lastUsedAt = account.getLastUsed();
		this.callerType = account.getCallerType();
		this.referer = account.getReferrer();
	}

	public static class DatarouterAccountFielder extends BaseDatabeanFielder<DatarouterAccountKey,DatarouterAccount>{

		public DatarouterAccountFielder(){
			super(DatarouterAccountKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccount account){
			return List.of(
					new LongEncodedField<>(FieldKeys.createdAt, account.createdAt),
					new StringField(FieldKeys.creator, account.creator),
					new LongEncodedField<>(FieldKeys.lastUsedAt, account.lastUsedAt),
					new BooleanField(FieldKeys.enableUserMappings, account.enableUserMappings),
					new StringField(FieldKeys.callerType, account.callerType),
					new StringField(FieldKeys.referer, account.referer));
		}

	}

	@Override
	public Supplier<DatarouterAccountKey> getKeySupplier(){
		return DatarouterAccountKey::new;
	}

	public String getCreatedDate(ZoneId zoneId){
		if(createdAt == null){
			return "";
		}
		return createdAt.format(zoneId);
	}

	public String getCreator(){
		return creator;
	}

	public void setLastUsed(MilliTime lastUsedAt){
		this.lastUsedAt = lastUsedAt;
	}

	public String getLastUsedDate(ZoneId zoneId){
		if(lastUsedAt == null){
			return "";
		}
		return lastUsedAt.format(zoneId);
	}

	public MilliTime getLastUsed(){
		return lastUsedAt;
	}

	public Instant getLastUsedInstant(){
		if(lastUsedAt == null){
			return Instant.MIN;
		}
		return lastUsedAt.toInstant();
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

	public MilliTime getCreated(){
		return createdAt;
	}

	public void setCallerType(String callerType){
		this.callerType = callerType;
	}

	public String getCallerType(){
		return callerType;
	}

	public String getReferrer(){
		return referer;
	}

	public void setReferrer(String referrer){
		this.referer = StringTool.isEmptyOrWhitespace(referrer) ? null : referrer.trim();
	}

}
