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
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZonedDateFormatterTool;

public class DatarouterAccount extends BaseDatabean<DatarouterAccountKey,DatarouterAccount>{

	private Date created;
	private String creator;
	private Date lastUsed;
	private Boolean enableUserMappings;
	public String callerType; // IN-9144
	/*
	 * The official spec spells this as "referer"
	 */
	public String referrer; // IN-8049

	private static class FieldKeys{
		@SuppressWarnings("deprecation")
		private static final DateFieldKey created = new DateFieldKey("created");
		private static final StringFieldKey creator = new StringFieldKey("creator");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey lastUsed = new DateFieldKey("lastUsed");
		private static final BooleanFieldKey enableUserMappings = new BooleanFieldKey("enableUserMappings");
		private static final StringFieldKey callerType = new StringFieldKey("callerType");
		private static final StringFieldKey referrer = new StringFieldKey("referrer");
	}

	public DatarouterAccount(){
		super(new DatarouterAccountKey());
	}

	public DatarouterAccount(String accountName, Date created, String creator){
		super(new DatarouterAccountKey(accountName));
		this.created = created;
		this.creator = creator;
		this.enableUserMappings = false;
		this.callerType = null;
		this.referrer = null;
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
		this(accountName, account.getCreated(), account.getCreator());
		this.enableUserMappings = account.getEnableUserMappings();
		this.lastUsed = account.getLastUsed();
		this.callerType = account.getCallerType();
		this.referrer = account.getReferrer();
	}

	public static class DatarouterAccountFielder extends BaseDatabeanFielder<DatarouterAccountKey,DatarouterAccount>{

		public DatarouterAccountFielder(){
			super(DatarouterAccountKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccount account){
			return List.of(
					new DateField(FieldKeys.created, account.created),
					new StringField(FieldKeys.creator, account.creator),
					new DateField(FieldKeys.lastUsed, account.lastUsed),
					new BooleanField(FieldKeys.enableUserMappings, account.enableUserMappings),
					new StringField(FieldKeys.callerType, account.callerType),
					new StringField(FieldKeys.referrer, account.referrer));
		}

	}

	@Override
	public Supplier<DatarouterAccountKey> getKeySupplier(){
		return DatarouterAccountKey::new;
	}

	public String getCreatedDate(ZoneId zoneId){
		if(created == null){
			return "";
		}
		return ZonedDateFormatterTool.formatDateWithZone(created, zoneId);
	}

	public String getCreator(){
		return creator;
	}

	public void setLastUsed(Date lastUsed){
		this.lastUsed = lastUsed;
	}

	public String getLastUsedDate(ZoneId zoneId){
		if(lastUsed == null){
			return "";
		}
		return ZonedDateFormatterTool.formatDateWithZone(lastUsed, zoneId);
	}

	public Date getLastUsed(){
		return lastUsed;
	}

	public Instant getLastUsedInstant(){
		if(lastUsed == null){
			return Instant.MIN;
		}
		return lastUsed.toInstant();
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

	public Date getCreated(){
		return created;
	}

	public void setCallerType(String callerType){
		this.callerType = callerType;
	}

	public String getCallerType(){
		return callerType;
	}

	public String getReferrer(){
		return referrer;
	}

	public void setReferrer(String referrer){
		this.referrer = StringTool.isEmptyOrWhitespace(referrer) ? null : referrer.trim();
	}

}
