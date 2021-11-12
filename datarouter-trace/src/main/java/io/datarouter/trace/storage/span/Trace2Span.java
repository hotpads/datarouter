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
import java.util.function.Supplier;

import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.trace.storage.trace.Trace2;

public class Trace2Span
extends BaseDatabean<Trace2SpanKey,Trace2Span>{

	private Integer parentSequence;
	private String name;
	private Trace2SpanGroupType groupType;
	private Long created;
	private Long ended;
	private String info;
	private Long cpuTimeCreatedNs;
	private Long cpuTimeEndedNs;
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesEnded;

	public static class FieldKeys{
		public static final IntegerFieldKey parentSequence = new IntegerFieldKey("parentSequence")
				.withColumnName("parentSequence2");
		public static final StringFieldKey name = new StringFieldKey("name")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEnumFieldKey<Trace2SpanGroupType> groupType = new StringEnumFieldKey<>("groupType",
				Trace2SpanGroupType.class);
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final LongFieldKey created = new LongFieldKey("created")
				.withColumnName("created2");
		public static final LongFieldKey ended = new LongFieldKey("ended")
				.withColumnName("ended2");
		public static final LongFieldKey cpuTimeCreatedNs = new LongFieldKey("cpuTimeCreatedNs")
				.withColumnName("cpuTimeCreatedNs2");
		public static final LongFieldKey cpuTimeEndedNs = new LongFieldKey("cpuTimeEndedNs")
				.withColumnName("cpuTimeEndedNs2");
		public static final LongFieldKey memoryAllocatedBytesBegin = new LongFieldKey("memoryAllocatedBytesBegin")
				.withColumnName("memoryAllocatedBytesBegin2");
		public static final LongFieldKey memoryAllocatedBytesEnded = new LongFieldKey("memoryAllocatedBytesEnded")
				.withColumnName("memoryAllocatedBytesEnded2");
	}

	public static class Trace2SpanFielder extends BaseDatabeanFielder<Trace2SpanKey,Trace2Span>{

		public Trace2SpanFielder(){
			super(Trace2SpanKey::new);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Span databean){
			return List.of(
					new IntegerField(FieldKeys.parentSequence, databean.parentSequence),
					new StringField(FieldKeys.name, databean.name),
					new StringEnumField<>(FieldKeys.groupType, databean.groupType),
					new StringField(FieldKeys.info, databean.info),
					new LongField(FieldKeys.created, databean.created),
					new LongField(FieldKeys.ended, databean.ended),
					new LongField(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new LongField(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new LongField(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new LongField(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded));
		}
	}

	public Trace2Span(){
		this(new Trace2SpanKey());
	}

	public Trace2Span(Trace2SpanKey key){
		super(key);
	}

	public Trace2Span(Trace2SpanDto dto){
		super(new Trace2SpanKey(dto.traceparent, dto.parentThreadId, dto.sequence));
		this.parentSequence = dto.parentSequence;
		this.name = dto.name;
		this.groupType = dto.groupType != null
				? Trace2SpanGroupType.fromPersistentStringStatic(dto.groupType.type)
				: null;
		this.created = dto.created;
		this.ended = dto.getEnded();
		this.info = dto.getInfo();
		this.cpuTimeCreatedNs = dto.getCpuTimeCreatedNs();
		this.cpuTimeEndedNs = dto.getCpuTimeEndedNs();
		this.memoryAllocatedBytesBegin = dto.getMemoryAllocatedBytesBegin();
		this.memoryAllocatedBytesEnded = dto.getMemoryAllocatedBytesEnded();
	}

	@Override
	public Supplier<Trace2SpanKey> getKeySupplier(){
		return Trace2SpanKey::new;
	}

	public boolean isTopLevel(){
		return this.parentSequence == null;
	}

	public Long getThreadId(){
		return getKey().getThreadId();
	}

	public Integer getSequence(){
		return getKey().getSequence();
	}

	public String getName(){
		return name;
	}

	public Trace2SpanGroupType getGroupType(){
		return groupType;
	}

	public Long getCreatedNs(){
		return created;
	}

	public Long getEndedNs(){
		return ended;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public String getInfo(){
		return info;
	}

	public Long getDurationNs(){
		return ended - created;
	}

	public Traceparent getTraceparent(){
		return new Traceparent(getKey().getEntityKey().getTrace2EntityId(), getKey().getParentId());
	}

	@Override
	public String toString(){
		return getKey() + "[" + name + "][" + info + "]";
	}

}
