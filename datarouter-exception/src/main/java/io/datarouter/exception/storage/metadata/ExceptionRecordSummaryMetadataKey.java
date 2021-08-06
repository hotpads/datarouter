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
package io.datarouter.exception.storage.metadata;

public class ExceptionRecordSummaryMetadataKey
extends BaseExceptionRecordSummaryMetadataKey<ExceptionRecordSummaryMetadataKey>{

	private static final String UNKNOWN_EXCEPTION_LOCATION = "unknown";

	public ExceptionRecordSummaryMetadataKey(){
	}

	public ExceptionRecordSummaryMetadataKey(String type, String exceptionLocation){
		super(type, exceptionLocation);
	}

	public static ExceptionRecordSummaryMetadataKey createWithNullableLocation(String type, String exceptionLocation){
		String nonNullLocation = exceptionLocation != null ? exceptionLocation : UNKNOWN_EXCEPTION_LOCATION;
		return new ExceptionRecordSummaryMetadataKey(type, nonNullLocation);
	}

}
