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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;

public abstract class BaseExceptionRecordSummaryMetadata<
		PK extends BaseExceptionRecordSummaryMetadataKey<PK>,
		D extends BaseExceptionRecordSummaryMetadata<PK,D>>
extends BaseDatabean<PK,D>{

	private String issue;
	private Boolean muted;

	public static class FieldKeys{
		public static final StringFieldKey issue = new StringFieldKey("issue");
		public static final BooleanFieldKey muted = new BooleanFieldKey("muted");
	}

	public BaseExceptionRecordSummaryMetadata(PK key){
		super(key);
	}

	public static class BaseExceptionRecordSummaryMetadataFielder<
			PK extends BaseExceptionRecordSummaryMetadataKey<PK>,
			D extends BaseExceptionRecordSummaryMetadata<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseExceptionRecordSummaryMetadataFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
			super(primaryKeyFielderSupplier);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D metadata){
			return List.of(
					new StringField(FieldKeys.issue, metadata.getIssue()),
					new BooleanField(FieldKeys.muted, metadata.getMuted()));
		}

	}

	public String getIssue(){
		return issue;
	}

	public void setIssue(String issue){
		this.issue = issue;
	}

	public Boolean getMuted(){
		return muted;
	}

	public void setMuted(Boolean muted){
		this.muted = muted;
	}

}
