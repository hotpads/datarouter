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
package io.datarouter.trace.storage.span;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.trace.storage.entity.BaseTraceEntityKey;

public abstract class BaseTraceSpanKey<
		EK extends BaseTraceEntityKey<EK>,
		PK extends BaseTraceSpanKey<EK,PK>>
extends BaseEntityPrimaryKey<EK,PK>{

	protected EK entityKey;

	protected Long threadId;
	protected Integer sequence;

	public static class FieldKeys{
		public static final LongFieldKey threadId = new LongFieldKey("threadId");
		public static final IntegerFieldKey sequence = new IntegerFieldKey("sequence");
	}

	public BaseTraceSpanKey(){
	}

	public BaseTraceSpanKey(Long threadId, Integer sequence){
		this.threadId = threadId;
		this.sequence = sequence;
	}

	@Override
	public EK getEntityKey(){
		return entityKey;
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new LongField(FieldKeys.threadId, threadId),
				new IntegerField(FieldKeys.sequence, sequence));
	}

	public Long getThreadId(){
		return threadId;
	}

	public Integer getSequence(){
		return sequence;
	}

}
