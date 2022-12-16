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
package io.datarouter.exception.conveyors;

import java.util.Collection;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.queue.configuration.BaseMemoryBufferPutMultiConsumerConveyorConfiguration;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;

@Singleton
public class HttpRequestRecordMemoryToDatabaseConveyorConfiguration
extends BaseMemoryBufferPutMultiConsumerConveyorConfiguration<HttpRequestRecord>{

	@Inject
	private DatarouterExceptionBuffers buffers;
	@Inject
	private DatarouterHttpRequestRecordDao httpRequestRecordDao;

	@Override
	protected MemoryBuffer<HttpRequestRecord> getMemoryBuffer(){
		return buffers.httpRequestRecordBuffer;
	}

	@Override
	protected Consumer<Collection<HttpRequestRecord>> getPutMultiConsumer(){
		return httpRequestRecordDao::putMulti;
	}

}
