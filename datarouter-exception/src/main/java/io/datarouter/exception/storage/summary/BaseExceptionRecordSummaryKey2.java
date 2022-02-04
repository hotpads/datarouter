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

import java.util.List;
import java.util.Objects;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public abstract class BaseExceptionRecordSummaryKey2<PK extends RegularPrimaryKey<PK>>extends BaseRegularPrimaryKey<PK>{

	private Long reversePeriodStart;
	private String nameHash;

	public static class FieldKeys{
		public static final LongFieldKey reversePeriodStart = new LongFieldKey("reversePeriodStart");
		public static final StringFieldKey nameHash = new StringFieldKey("nameHash");
	}

	public BaseExceptionRecordSummaryKey2(){
	}

	public BaseExceptionRecordSummaryKey2(Long periodStart, String name){
		this.reversePeriodStart = periodStart != null ? Long.MAX_VALUE - periodStart : null;
		this.nameHash = makeHashcodeString(name);
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new LongField(FieldKeys.reversePeriodStart, reversePeriodStart),
				new StringField(FieldKeys.nameHash, nameHash));
	}

	public String makeHashcodeString(String name){
		return String.valueOf(Objects.hash(name));
	}

	public long getPeriodStart(){
		return Long.MAX_VALUE - reversePeriodStart;
	}

	public long getReversePeriodStart(){
		return reversePeriodStart;
	}

	public String getNameHash(){
		return nameHash;
	}

}
