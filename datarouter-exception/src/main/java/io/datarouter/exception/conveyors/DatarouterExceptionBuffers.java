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
package io.datarouter.exception.conveyors;

import javax.inject.Singleton;

import io.datarouter.conveyor.DatabeanBuffer;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordKey;

@Singleton
public class DatarouterExceptionBuffers{

	private static final int MAX_SIZE = 10_000;

	public final DatabeanBuffer<ExceptionRecordKey,ExceptionRecord> exceptionRecordBuffer;
	public final DatabeanBuffer<HttpRequestRecordKey,HttpRequestRecord> httpRequestRecordBuffer;

	public DatarouterExceptionBuffers(){
		this.exceptionRecordBuffer = new DatabeanBuffer<>("exceptionRecord", MAX_SIZE);
		this.httpRequestRecordBuffer = new DatabeanBuffer<>("httpRequestRecord", MAX_SIZE);
	}

}
