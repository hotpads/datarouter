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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;

public abstract class BaseExceptionRecordSummary<
		PK extends BaseExceptionRecordSummaryKey<PK>,
		D extends BaseExceptionRecordSummary<PK,D>>
extends BaseDatabean<PK,D>{

	private Long numExceptions;
	private String sampleExceptionRecordId;

	public static class FieldKeys{
		public static final LongFieldKey numExceptions = new LongFieldKey("numExceptions");
		public static final StringFieldKey sampleExceptionRecordId = new StringFieldKey("sampleExceptionRecordId");
	}

	public abstract static class BaseExceptionRecordSummaryFielder<
			PK extends BaseExceptionRecordSummaryKey<PK>,
			D extends BaseExceptionRecordSummary<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseExceptionRecordSummaryFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
			super(primaryKeyFielderSupplier);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return List.of(
					new LongField(FieldKeys.numExceptions, databean.getNumExceptions()),
					new StringField(FieldKeys.sampleExceptionRecordId, databean.getSampleExceptionRecordId()));
		}

	}

	public BaseExceptionRecordSummary(PK key, Long numExceptions, String sampleExceptionRecordId){
		super(key);
		this.numExceptions = numExceptions;
		this.sampleExceptionRecordId = sampleExceptionRecordId;
	}

	public Long getNumExceptions(){
		return numExceptions;
	}

	public String getSampleExceptionRecordId(){
		return sampleExceptionRecordId;
	}

}
