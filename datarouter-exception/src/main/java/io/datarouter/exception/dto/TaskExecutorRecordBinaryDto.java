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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.util.Require;

public class TaskExecutorRecordBinaryDto
extends BinaryDto<TaskExecutorRecordBinaryDto>
implements TaskExecutionRecordBinaryDto<TaskExecutorRecordDto>{

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final String id;
	@BinaryDtoField(index = 2)
	public final String traceId;
	@BinaryDtoField(index = 3)
	public final String parentId;
	@BinaryDtoField(index = 4)
	public final String exceptionRecordId;

	public TaskExecutorRecordBinaryDto(
			String serviceName,
			TaskExecutorRecordDto record){
		this.serviceName = Require.notBlank(serviceName);
		this.id = record.id();
		this.traceId = record.traceId();
		this.parentId = record.parentId();
		this.exceptionRecordId = record.exceptionRecordId();
	}

	@Override
	public TaskExecutorRecordDto toDto(){
		return new TaskExecutorRecordDto(
				id,
				traceId,
				parentId,
				exceptionRecordId);
	}

	public static TaskExecutorRecordBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(TaskExecutorRecordBinaryDto.class).decode(bytes);
	}

	@Override
	public String getServiceName(){
		return serviceName;
	}

}
