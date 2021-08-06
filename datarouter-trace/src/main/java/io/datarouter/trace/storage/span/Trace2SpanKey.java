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
package io.datarouter.trace.storage.span;

import java.util.List;

import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.trace.storage.entity.Trace2EntityKey;

public class Trace2SpanKey
extends BaseEntityPrimaryKey<Trace2EntityKey,Trace2SpanKey>{

	private Trace2EntityKey entityKey;
	private String parentId;
	private Long threadId;
	private Integer sequence;

	public static class FieldKeys{
		public static final StringFieldKey parentId = new StringFieldKey("parentId");
		public static final LongFieldKey threadId = new LongFieldKey("threadId");
		public static final IntegerFieldKey sequence = new IntegerFieldKey("sequence");
	}


	public Trace2SpanKey(){
		this.entityKey = new Trace2EntityKey();
	}

	public Trace2SpanKey(Trace2EntityKey entityKey){
		this.entityKey = entityKey;
	}

	public Trace2SpanKey(Traceparent traceparent, Long threadId, Integer sequence){
		this.entityKey = new Trace2EntityKey(traceparent);
		this.parentId = traceparent.parentId;
		this.threadId = threadId;
		this.sequence = sequence;
	}

	@Override
	public Trace2SpanKey prefixFromEntityKey(Trace2EntityKey entityKey){
		return new Trace2SpanKey(entityKey);
	}

	@Override
	public Trace2EntityKey getEntityKey(){
		return entityKey;
	}


	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return List.of(
				new StringField(FieldKeys.parentId, parentId),
				new LongField(FieldKeys.threadId, threadId),
				new IntegerField(FieldKeys.sequence, sequence));
	}

	public String getParentId(){
		return parentId;
	}

	public Long getThreadId(){
		return threadId;
	}

	public Integer getSequence(){
		return sequence;
	}

}
