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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.exception.storage.httprecord.FieldTrimTool;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.service.DatarouterServiceFieldKeys;

public class ExceptionRecord extends BaseDatabean<ExceptionRecordKey,ExceptionRecord>{

	private MilliTime createdAt;
	private String serviceName;
	private String serverName;
	// exception category, e.g. job, joblet, web(http request or node), conveyor and etc.
	private String category;
	// smart exception naming, e.g. "{$serverName} error", "read timeout", "null pointer exception" and etc.
	private String name;
	private String stackTrace;
	// class name of the Throwable, e.g. java.lang.IllegalArgumentException, java.lang.RuntimeException
	private String type;
	private String appVersion;
	// the exception root cause class which has path prefix as "io.datarouter" or "com.hotpads"
	private String exceptionLocation;
	private String methodName;
	private Integer lineNumber;
	// the caller that's getting the exception
	private String callOrigin;
	private List<String> additionalAlertRecipients; // not persisted

	public static class FieldKeys{
		public static final LongEncodedFieldKey<MilliTime> createdAt = new LongEncodedFieldKey<>("createdAt",
				new MilliTimeFieldCodec());
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final StringFieldKey category = new StringFieldKey("category");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey stackTrace = new StringFieldKey("stackTrace")
				.withSize(CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT);
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey appVersion = new StringFieldKey("appVersion");
		public static final StringFieldKey exceptionLocation = new StringFieldKey("exceptionLocation");
		public static final StringFieldKey methodName = new StringFieldKey("methodName");
		public static final IntegerFieldKey lineNumber = new IntegerFieldKey("lineNumber");
		public static final StringFieldKey callOrigin = new StringFieldKey("callOrigin");
	}

	public static class ExceptionRecordFielder extends BaseDatabeanFielder<ExceptionRecordKey,ExceptionRecord>{

		public ExceptionRecordFielder(){
			super(ExceptionRecordKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ExceptionRecord databean){
			return List.of(
					new LongEncodedField<>(FieldKeys.createdAt, databean.createdAt),
					new StringField(DatarouterServiceFieldKeys.serviceName, databean.serviceName),
					new StringField(FieldKeys.serverName, databean.serverName),
					new StringField(FieldKeys.category, databean.category),
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.stackTrace, databean.stackTrace),
					new StringField(FieldKeys.type, databean.type),
					new StringField(FieldKeys.appVersion, databean.appVersion),
					new StringField(FieldKeys.exceptionLocation, databean.exceptionLocation),
					new StringField(FieldKeys.methodName, databean.methodName),
					new IntegerField(FieldKeys.lineNumber, databean.lineNumber),
					new StringField(FieldKeys.callOrigin, databean.callOrigin));
			}

	}

	public ExceptionRecord(){
		super(new ExceptionRecordKey());
	}

	public ExceptionRecord(
			ExceptionRecordKey key,
			long dateMs,
			String serviceName,
			String serverName,
			String category,
			String name,
			String stackTrace,
			String type,
			String appVersion,
			String exceptionLocation,
			String methodName,
			Integer lineNumber,
			String callOrigin,
			List<String> additionalAlertRecipients){
		super(key);
		this.createdAt = MilliTime.ofEpochMilli(dateMs);
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.category = category;
		this.name = name;
		this.stackTrace = stackTrace;
		this.type = type;
		this.appVersion = appVersion;
		this.exceptionLocation = exceptionLocation;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.callOrigin = callOrigin;
		this.additionalAlertRecipients = additionalAlertRecipients;
	}

	public ExceptionRecord(ExceptionRecordDto dto){
		this(new ExceptionRecordKey(dto.id()),
				dto.created().getTime(),
				dto.serviceName(),
				dto.serverName(),
				dto.category(),
				dto.name(),
				dto.stackTrace(),
				dto.type(),
				dto.appVersion(),
				dto.exceptionLocation(),
				dto.methodName(),
				dto.lineNumber(),
				dto.callOrigin(),
				dto.additionalAlertRecipients());
	}

	@Override
	public Supplier<ExceptionRecordKey> getKeySupplier(){
		return ExceptionRecordKey::new;
	}

	public ExceptionRecordDto toDto(){
		return new ExceptionRecordDto(
				getKey().getId(),
				createdAt.toDate(),
				serviceName,
				serverName,
				category,
				name,
				stackTrace,
				type,
				appVersion,
				exceptionLocation,
				methodName,
				lineNumber,
				callOrigin,
				additionalAlertRecipients);
	}

	public MilliTime getCreated(){
		return createdAt;
	}

	public String getServiceName(){
		return serviceName;
	}

	public String getServerName(){
		return serverName;
	}

	public String getCategory(){
		return category;
	}

	public String getName(){
		return name;
	}

	public String getStackTrace(){
		return stackTrace;
	}

	public void setStackTrace(String stackTrace){
		this.stackTrace = stackTrace;
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

	public List<String> getAdditionalAlertRecipients(){
		return additionalAlertRecipients;
	}

	public void trimFields(){
		trimStackTrace();
	}

	public void trimStackTrace(){
		stackTrace = StringTool.trimToSizeAndLog(
				stackTrace,
				ExceptionRecordDto.STACK_TRACE_LENGTH_LIMIT,
				FieldTrimTool.TRIMMING_REPLACEMENT,
				"field=stackTrace",
				"exceptionRecordId=" + getKey().getId());
	}

}
