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

import java.util.Arrays;
import java.util.List;

import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public abstract class BaseExceptionRecordSummaryKey<PK extends RegularPrimaryKey<PK>> extends BaseRegularPrimaryKey<PK>{

	private Long reversePeriodStart;
	private String type;
	private String exceptionLocation;

	public static class FieldKeys{
		public static final LongFieldKey reversePeriodStart = new LongFieldKey("reversePeriodStart");
	}

	public BaseExceptionRecordSummaryKey(){
	}

	public BaseExceptionRecordSummaryKey(long periodStart, String type, String exceptionLocation){
		this.reversePeriodStart = Long.MAX_VALUE - periodStart;
		this.type = type;
		this.exceptionLocation = exceptionLocation;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new LongField(FieldKeys.reversePeriodStart, reversePeriodStart),
				new StringField(ExceptionRecord.FieldKeys.type, type),
				new StringField(ExceptionRecord.FieldKeys.exceptionLocation, exceptionLocation));
	}

	public long getPeriodStart(){
		return Long.MAX_VALUE - reversePeriodStart;
	}

	public long getReversePeriodStart(){
		return reversePeriodStart;
	}

	public String getType(){
		return type;
	}

	public String getExceptionLocation(){
		return exceptionLocation;
	}

}
