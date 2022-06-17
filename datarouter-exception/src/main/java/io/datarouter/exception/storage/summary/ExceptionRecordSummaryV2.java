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
package io.datarouter.exception.storage.summary;

import java.util.function.Supplier;

public class ExceptionRecordSummaryV2
extends BaseExceptionRecordSummary2<ExceptionRecordSummaryKeyV2,ExceptionRecordSummaryV2>{

	public static class ExceptionRecordSummaryV2Fielder
	extends BaseExceptionRecordSummary2Fielder<ExceptionRecordSummaryKeyV2,ExceptionRecordSummaryV2>{

		public ExceptionRecordSummaryV2Fielder(){
			super(ExceptionRecordSummaryKeyV2::new);
		}

	}

	public ExceptionRecordSummaryV2(){
		this(new ExceptionRecordSummaryKeyV2(), "", "", 0L, "");
	}

	public ExceptionRecordSummaryV2(ExceptionRecordSummaryKeyV2 key, String name, String category, Long numExceptions,
			String sampleExceptionRecordId){
		super(key, name, category, numExceptions, sampleExceptionRecordId);
	}

	@Override
	public Supplier<ExceptionRecordSummaryKeyV2> getKeySupplier(){
		return ExceptionRecordSummaryKeyV2::new;
	}

}
