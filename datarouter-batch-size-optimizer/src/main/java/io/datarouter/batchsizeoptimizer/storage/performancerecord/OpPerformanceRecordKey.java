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
package io.datarouter.batchsizeoptimizer.storage.performancerecord;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class OpPerformanceRecordKey extends BaseRegularPrimaryKey<OpPerformanceRecordKey>{

	private String opName;
	private Long timestamp;
	private Long nanotime;

	public static class FieldKeys{
		public static final StringFieldKey opName = new StringFieldKey("opName");
		public static final LongFieldKey timestamp = new LongFieldKey("timestamp");
		public static final LongFieldKey nanotime = new LongFieldKey("nanotime");
	}

	public OpPerformanceRecordKey(String opName, Long timestamp, Long nanotime){
		this.opName = opName;
		this.timestamp = timestamp;
		this.nanotime = nanotime;
	}

	public OpPerformanceRecordKey(){
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.opName, opName),
				new LongField(FieldKeys.timestamp, timestamp),
				new LongField(FieldKeys.nanotime, nanotime));
	}

	public Long getTimestamp(){
		return timestamp;
	}

	public String getOpName(){
		return opName;
	}

}
