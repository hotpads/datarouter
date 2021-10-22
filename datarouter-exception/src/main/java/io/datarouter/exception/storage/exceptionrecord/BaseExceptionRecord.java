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
package io.datarouter.exception.storage.exceptionrecord;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.exception.storage.httprecord.FieldTrimTool;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.web.service.DatarouterServiceFieldKeys;

public abstract class BaseExceptionRecord<
		PK extends BaseExceptionRecordKey<PK>,
		D extends BaseExceptionRecord<PK,D>>
extends BaseDatabean<PK,D>{

	private Date created;
	private String serviceName;
	private String serverName;
	private String stackTrace;
	private String type;
	private String appVersion;
	private String exceptionLocation;
	private String methodName;
	private Integer lineNumber;
	private String callOrigin;

	public static class FieldKeys{
		@SuppressWarnings("deprecation")
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final StringFieldKey stackTrace = new StringFieldKey("stackTrace")
				.withSize(CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT);
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey appVersion = new StringFieldKey("appVersion");
		public static final StringFieldKey exceptionLocation = new StringFieldKey("exceptionLocation");
		public static final StringFieldKey methodName = new StringFieldKey("methodName");
		public static final IntegerFieldKey lineNumber = new IntegerFieldKey("lineNumber");
		public static final StringFieldKey callOrigin = new StringFieldKey("callOrigin");
	}

	public abstract static class BaseExceptionRecordFielder<
			PK extends BaseExceptionRecordKey<PK>,
			D extends BaseExceptionRecord<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseExceptionRecordFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
			super(primaryKeyFielderSupplier);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return List.of(
					new DateField(FieldKeys.created, databean.getCreated()),
					new StringField(DatarouterServiceFieldKeys.serviceName, databean.getServiceName()),
					new StringField(FieldKeys.serverName, databean.getServerName()),
					new StringField(FieldKeys.stackTrace, databean.getStackTrace()),
					new StringField(FieldKeys.type, databean.getType()),
					new StringField(FieldKeys.appVersion, databean.getAppVersion()),
					new StringField(FieldKeys.exceptionLocation, databean.getExceptionLocation()),
					new StringField(FieldKeys.methodName, databean.getMethodName()),
					new IntegerField(FieldKeys.lineNumber, databean.getLineNumber()),
					new StringField(FieldKeys.callOrigin, databean.getCallOrigin()));
			}
	}


	public BaseExceptionRecord(PK key){
		super(key);
	}

	public BaseExceptionRecord(
			PK key,
			String serviceName,
			String serverName,
			String stackTrace,
			String type,
			String appVersion,
			String exceptionLocation,
			String methodName,
			Integer lineNumber,
			String callOrigin){
		this(key,
				System.currentTimeMillis(),
				serviceName,
				serverName,
				stackTrace,
				type,
				appVersion,
				exceptionLocation,
				methodName,
				lineNumber,
				callOrigin);
	}

	public BaseExceptionRecord(
			PK key,
			long dateMs,
			String serviceName,
			String serverName,
			String stackTrace,
			String type,
			String appVersion,
			String exceptionLocation,
			String methodName,
			Integer lineNumber,
			String callOrigin){
		super(key);
		this.created = new Date(dateMs);
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.stackTrace = stackTrace;
		this.type = type;
		this.appVersion = appVersion;
		this.exceptionLocation = exceptionLocation;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.callOrigin = callOrigin;
	}

	public BaseExceptionRecord(PK key, ExceptionRecordDto exceptionRecordDto){
		super(key);
		this.created = exceptionRecordDto.created;
		this.serviceName = exceptionRecordDto.serviceName;
		this.serverName = exceptionRecordDto.serverName;
		this.stackTrace = exceptionRecordDto.stackTrace;
		this.type = exceptionRecordDto.type;
		this.appVersion = exceptionRecordDto.appVersion;
		this.exceptionLocation = exceptionRecordDto.exceptionLocation;
		this.methodName = exceptionRecordDto.methodName;
		this.lineNumber = exceptionRecordDto.lineNumber;
		this.callOrigin = exceptionRecordDto.callOrigin;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public String getServiceName(){
		return serviceName;
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

	public String getMethodName(){
		return methodName;
	}

	public Integer getLineNumber(){
		return lineNumber;
	}

	public String getCallOrigin(){
		return callOrigin;
	}

	public void trimFields(){
		trimstackTrace();
	}

	public void trimstackTrace(){
		stackTrace = trimField(FieldKeys.stackTrace, stackTrace);
	}

	private String trimField(StringFieldKey fieldKey, String field){
		return FieldTrimTool.trimField(fieldKey, field, "exceptionRecordId=" + getKey().getId());
	}

}
