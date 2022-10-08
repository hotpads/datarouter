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
package io.datarouter.exception.dto;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.util.Require;

public class ExceptionRecordBinaryDto extends BinaryDto<ExceptionRecordBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final String serverName;
	@BinaryDtoField(index = 2)
	public final String appVersion;
	@BinaryDtoField(index = 3)
	public final String id;
	@BinaryDtoField(index = 4)
	public final Instant created;
	@BinaryDtoField(index = 5)
	public final String category;
	@BinaryDtoField(index = 6)
	public final String name;
	@BinaryDtoField(index = 7)
	public final String stackTrace;
	@BinaryDtoField(index = 8)
	public final String type;
	@BinaryDtoField(index = 9)
	public final String exceptionLocation;
	@BinaryDtoField(index = 10)
	public final String methodName;
	@BinaryDtoField(index = 11)
	public final Integer lineNumber;
	@BinaryDtoField(index = 12)
	public final String callOrigin;
	@BinaryDtoField(index = 13)
	public final List<String> additionalAlertRecipients;

	public ExceptionRecordBinaryDto(
			String serviceName,
			String serverName,
			String appVersion,
			String id,
			Date created,
			String category,
			String name,
			String stackTrace,
			String type,
			String exceptionLocation,
			String methodName,
			Integer lineNumber,
			String callOrigin,
			List<String> additionalAlertRecipients){
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.appVersion = Require.notBlank(appVersion);
		this.id = id;
		this.created = created == null ? null : created.toInstant();
		this.category = category;
		this.name = name;
		this.stackTrace = stackTrace;
		this.type = type;
		this.exceptionLocation = exceptionLocation;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.callOrigin = callOrigin;
		this.additionalAlertRecipients = additionalAlertRecipients;
	}

	public ExceptionRecordBinaryDto(ExceptionRecordDto dto){
		this(
				dto.serviceName(),
				dto.serverName(),
				dto.appVersion(),
				dto.id(),
				dto.created(),
				dto.category(),
				dto.name(),
				dto.stackTrace(),
				dto.type(),
				dto.exceptionLocation(),
				dto.methodName(),
				dto.lineNumber(),
				dto.callOrigin(),
				dto.additionalAlertRecipients());
	}

	public ExceptionRecordDto toDto(){
		return new ExceptionRecordDto(
				id,
				created == null ? null : Date.from(created),
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

	public static ExceptionRecordBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(ExceptionRecordBinaryDto.class).decode(bytes);
	}

}
