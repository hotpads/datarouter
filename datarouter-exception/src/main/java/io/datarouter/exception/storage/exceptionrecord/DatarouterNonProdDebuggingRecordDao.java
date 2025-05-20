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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.exception.dto.ExceptionRecordBinaryDto;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.exception.dto.TaskExecutorRecordBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import io.datarouter.trace.storage.binarydto.TraceQueueBinaryDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNonProdDebuggingRecordDao extends BaseDao{

	public record DatarouterNonProdExceptionRecordParams(ClientId clientId){

	}

	private final BlobQueueStorageNode<DatarouterDebuggingRecordGroupBinaryDto> node;

	@Inject
	public DatarouterNonProdDebuggingRecordDao(
			Datarouter datarouter,
			DatarouterNonProdExceptionRecordParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = queueNodeFactory
				.createBlobQueue(
						params.clientId,
						"DatarouterDebuggingRecord",
						BinaryDtoIndexedCodec.of(DatarouterDebuggingRecordGroupBinaryDto.class))
				.withNamespace("nonprod-shared")
				.withTag(Tag.DATAROUTER)
				.buildAndRegister();
	}

	public void combineAndPut(List<DatarouterDebuggingRecordGroupBinaryDto> dtos){
		Scanner.of(dtos)
				.then(node::combineAndPut);
	}

	public BlobQueueConsumer<DatarouterDebuggingRecordGroupBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

	public static class DatarouterDebuggingRecordGroupBinaryDto
	extends BinaryDto<DatarouterDebuggingRecordGroupBinaryDto>{

		@BinaryDtoField(index = 0)
		public final String environment;
		@BinaryDtoField(index = 1)
		public final String serviceName;
		@BinaryDtoField(index = 2)
		public final String serverName;
		@BinaryDtoField(index = 3)
		public final List<ExceptionRecordBinaryDto> exceptions;
		@BinaryDtoField(index = 4)
		public final List<HttpRequestRecordBinaryDto> requests;
		@BinaryDtoField(index = 5)
		public final List<TraceQueueBinaryDto> traces;
		@BinaryDtoField(index = 6)
		public final List<TaskExecutorRecordBinaryDto> executorRecords;

		public DatarouterDebuggingRecordGroupBinaryDto(
				String environment,
				String serviceName,
				String serverName,
				List<ExceptionRecordBinaryDto> exceptions,
				List<HttpRequestRecordBinaryDto> requests,
				List<TraceQueueBinaryDto> traces,
				List<TaskExecutorRecordBinaryDto> executorRecords){
			this.environment = environment;
			this.serviceName = serviceName;
			this.serverName = serverName;
			this.exceptions = exceptions;
			this.requests = requests;
			this.traces = traces;
			this.executorRecords = executorRecords;
		}
	}
}
