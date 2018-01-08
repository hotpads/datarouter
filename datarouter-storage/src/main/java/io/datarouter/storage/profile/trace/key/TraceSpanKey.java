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
/**
 *
 */
package io.datarouter.storage.profile.trace.key;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;

public class TraceSpanKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceSpanKey>{

	private Long traceId;
	private Long threadId;
	private Integer sequence;

	public static class FieldsKeys{
		public static final LongFieldKey threadId = new LongFieldKey("threadId");
		public static final IntegerFieldKey sequence = new IntegerFieldKey("sequence");
	}

	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(traceId);
	}

	@Override
	public String getEntityKeyName(){
		return null;
	}

	@Override
	public TraceSpanKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceSpanKey(entityKey.getTraceId(), null, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new LongField(FieldsKeys.threadId, threadId),
				new IntegerField(FieldsKeys.sequence, sequence));
	}


	/****************************** constructor ********************************/

	TraceSpanKey(){
	}

	public TraceSpanKey(Long traceId, Long threadId, Integer sequence){
		this.traceId = traceId;
		this.threadId = threadId;
		this.sequence = sequence;
	}


	/****************************** get/set ********************************/

	public Long getTraceId(){
		return traceId;
	}

	public Long getThreadId(){
		return threadId;
	}

	public void setThreadId(Long threadId){
		this.threadId = threadId;
	}

	public Integer getSequence(){
		return sequence;
	}

}