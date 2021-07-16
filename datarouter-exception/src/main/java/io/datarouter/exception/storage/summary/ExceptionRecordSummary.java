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
package io.datarouter.exception.storage.summary;

import java.util.function.Supplier;

public class ExceptionRecordSummary
extends BaseExceptionRecordSummary<ExceptionRecordSummaryKey,ExceptionRecordSummary>{

	public static class ExceptionRecordSummaryFielder
	extends BaseExceptionRecordSummaryFielder<ExceptionRecordSummaryKey,ExceptionRecordSummary>{

		public ExceptionRecordSummaryFielder(){
			super(ExceptionRecordSummaryKey.class);
		}

	}

	public ExceptionRecordSummary(){
		this(new ExceptionRecordSummaryKey(), 0L, "");
	}

	public ExceptionRecordSummary(ExceptionRecordSummaryKey key, Long numExceptions, String sampleExceptionRecordId){
		super(key, numExceptions, sampleExceptionRecordId);
	}

	@Override
	public Supplier<ExceptionRecordSummaryKey> getKeySupplier(){
		return ExceptionRecordSummaryKey::new;
	}

}
