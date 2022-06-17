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

import java.util.function.Supplier;

import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.model.databean.Databean;
import io.datarouter.util.lang.ClassTool;

public class ExceptionRecord extends BaseExceptionRecord<ExceptionRecordKey,ExceptionRecord>{

	public static class ExceptionRecordFielder extends BaseExceptionRecordFielder<ExceptionRecordKey,ExceptionRecord>{

		public ExceptionRecordFielder(){
			super(ExceptionRecordKey::new);
		}

	}

	public ExceptionRecord(){
		super(new ExceptionRecordKey());
	}

	public ExceptionRecord(ExceptionRecordDto dto){
		super(new ExceptionRecordKey(dto.id), dto.created.getTime(), dto.serviceName, dto.serverName, dto.category,
				dto.name, dto.stackTrace, dto.type, dto.appVersion, dto.exceptionLocation, dto.methodName,
				dto.lineNumber, dto.callOrigin);
	}

	public ExceptionRecord(
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
			String callOrigin){
		this(System.currentTimeMillis(), serviceName, serverName, category, name, stackTrace, type, appVersion,
				exceptionLocation, methodName, lineNumber, callOrigin);
	}

	public ExceptionRecord(
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
			String callOrigin){
		super(ExceptionRecordKey.generate(), dateMs, serviceName, serverName, category, name, stackTrace, type,
				appVersion, exceptionLocation, methodName, lineNumber, callOrigin);
	}

	@Override
	public Supplier<ExceptionRecordKey> getKeySupplier(){
		return ExceptionRecordKey::new;
	}

	@Override
	public int compareTo(Databean<?,?> that){
		int diff = ClassTool.compareClass(this, that);
		if(diff != 0){
			return diff;
		}
		return getCreated().compareTo(((ExceptionRecord)that).getCreated());
	}

	public ExceptionRecordDto toDto(){
		return new ExceptionRecordDto(
				getKey().getId(),
				getCreated(),
				getServiceName(),
				getServerName(),
				getCategory(),
				getName(),
				getStackTrace(),
				getType(),
				getAppVersion(),
				getExceptionLocation(),
				getMethodName(),
				getLineNumber(),
				getCallOrigin());
	}

}
