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
package io.datarouter.web.exception;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.model.util.CommonFieldSizes;

public abstract class BaseExceptionRecord<
		PK extends BaseExceptionRecordKey<PK>,
		D extends BaseExceptionRecord<PK,D>>
extends BaseDatabean<PK,D>{

	protected PK key;

	private Date created;
	private String serverName;
	private String stackTrace;
	private String type;
	private String appVersion;
	private String exceptionLocation;

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final StringFieldKey stackTrace = new StringFieldKey("stackTrace")
				.withSize(CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT);
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey appVersion = new StringFieldKey("appVersion");
		public static final StringFieldKey exceptionLocation = new StringFieldKey("exceptionLocation");
	}

	public abstract static class BaseExceptionRecordFielder<
			PK extends BaseExceptionRecordKey<PK>,
			D extends BaseExceptionRecord<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseExceptionRecordFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return Arrays.asList(
					new DateField(FieldKeys.created, databean.getCreated()),
					new StringField(FieldKeys.serverName, databean.getServerName()),
					new StringField(FieldKeys.stackTrace, databean.getStackTrace()),
					new StringField(FieldKeys.type, databean.getType()),
					new StringField(FieldKeys.appVersion, databean.getAppVersion()),
					new StringField(FieldKeys.exceptionLocation, databean.getExceptionLocation()));
			}
	}


	public BaseExceptionRecord(){
	}

	public BaseExceptionRecord(String serverName, String stackTrace, String type, String appVersion,
			String exceptionLocation){
		this(System.currentTimeMillis(), serverName, stackTrace, type, appVersion, exceptionLocation);
	}

	public BaseExceptionRecord(long dateMs, String serverName, String stackTrace, String type, String appVersion,
			String exceptionLocation){
		this.created = new Date(dateMs);
		this.serverName = serverName;
		this.stackTrace = stackTrace;
		this.type = type;
		this.appVersion = appVersion;
		this.exceptionLocation = exceptionLocation;
	}

	public BaseExceptionRecord(ExceptionRecordDto exceptionRecordDto){
		this.created = exceptionRecordDto.created;
		this.serverName = exceptionRecordDto.serverName;
		this.stackTrace = exceptionRecordDto.stackTrace;
		this.type = exceptionRecordDto.type;
		this.appVersion = exceptionRecordDto.appVersion;
		this.exceptionLocation = exceptionRecordDto.exceptionLocation;
	}


	@Override
	public PK getKey(){
		return key;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public String getServerName(){
		return serverName;
	}

	public String getStackTrace(){
		return stackTrace;
	}

	public String getType(){
		return type;
	}

	public String getAppVersion(){
		return appVersion;
	}

	public String getExceptionLocation(){
		return exceptionLocation;
	}

}
