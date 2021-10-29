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
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.trace.storage.trace.Trace2;

public class Trace2Span
extends BaseDatabean<Trace2SpanKey,Trace2Span>{

	@Deprecated
	private Integer parentSequence;
	private Integer parentSequence2;
	private String name;
	private Trace2SpanGroupType groupType;
	@Deprecated
	private Long created;
	private Long created2;
	@Deprecated
	private Long ended;
	private Long ended2;
	private String info;
	@Deprecated
	private Long cpuTimeCreatedNs;
	private Long cpuTimeCreatedNs2;
	@Deprecated
	private Long cpuTimeEndedNs;
	private Long cpuTimeEndedNs2;
	@Deprecated
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesBegin2;
	@Deprecated
	private Long memoryAllocatedBytesEnded;
	private Long memoryAllocatedBytesEnded2;

	public static class FieldKeys{
		@Deprecated
		public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
		public static final IntegerFieldKey parentSequence2 = new IntegerFieldKey("parentSequence2");
		public static final StringFieldKey name = new StringFieldKey("name")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEnumFieldKey<Trace2SpanGroupType> groupType = new StringEnumFieldKey<>("groupType",
				Trace2SpanGroupType.class);
		public static final StringFieldKey info = new StringFieldKey("info");
		@Deprecated
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final LongFieldKey created2 = new LongFieldKey("created2");
		@Deprecated
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
		public static final LongFieldKey ended2 = new LongFieldKey("ended2");
		@Deprecated
		public static final UInt63FieldKey cpuTimeCreatedNs = new UInt63FieldKey("cpuTimeCreatedNs");
		public static final LongFieldKey cpuTimeCreatedNs2 = new LongFieldKey("cpuTimeCreatedNs2");
		@Deprecated
		public static final UInt63FieldKey cpuTimeEndedNs = new UInt63FieldKey("cpuTimeEndedNs");
		public static final LongFieldKey cpuTimeEndedNs2 = new LongFieldKey("cpuTimeEndedNs2");
		@Deprecated
		public static final UInt63FieldKey memoryAllocatedBytesBegin = new UInt63FieldKey("memoryAllocatedBytesBegin");
		public static final LongFieldKey memoryAllocatedBytesBegin2 = new LongFieldKey("memoryAllocatedBytesBegin2");
		@Deprecated
		public static final UInt63FieldKey memoryAllocatedBytesEnded = new UInt63FieldKey("memoryAllocatedBytesEnded");
		public static final LongFieldKey memoryAllocatedBytesEnded2 = new LongFieldKey("memoryAllocatedBytesEnded2");
	}

	public static class Trace2SpanFielder extends BaseDatabeanFielder<Trace2SpanKey,Trace2Span>{

		public Trace2SpanFielder(){
			super(Trace2SpanKey::new);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Span databean){
			return List.of(
					new UInt31Field(FieldKeys.parentSequence, databean.parentSequence),
					new IntegerField(FieldKeys.parentSequence2, databean.parentSequence2),
					new StringField(FieldKeys.name, databean.name),
					new StringEnumField<>(FieldKeys.groupType, databean.groupType),
					new StringField(FieldKeys.info, databean.info),
					new UInt63Field(FieldKeys.created, databean.created),
					new LongField(FieldKeys.created2, databean.created2),
					new UInt63Field(FieldKeys.ended, databean.ended),
					new LongField(FieldKeys.ended2, databean.ended2),
					new UInt63Field(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new LongField(FieldKeys.cpuTimeCreatedNs2, databean.cpuTimeCreatedNs2),
					new UInt63Field(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new LongField(FieldKeys.cpuTimeEndedNs2, databean.cpuTimeEndedNs2),
					new UInt63Field(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new LongField(FieldKeys.memoryAllocatedBytesBegin2, databean.memoryAllocatedBytesBegin2),
					new UInt63Field(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded),
					new LongField(FieldKeys.memoryAllocatedBytesEnded2, databean.memoryAllocatedBytesEnded2));
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
		this.created2 = dto.created;
		this.ended = dto.getEnded();
		this.ended2 = dto.getEnded();
		this.info = dto.getInfo();
		this.cpuTimeCreatedNs = dto.getCpuTimeCreatedNs();
		this.cpuTimeCreatedNs2 = dto.getCpuTimeCreatedNs();
		this.cpuTimeEndedNs = dto.getCpuTimeEndedNs();
		this.cpuTimeEndedNs2 = dto.getCpuTimeEndedNs();
		this.memoryAllocatedBytesBegin = dto.getMemoryAllocatedBytesBegin();
		this.memoryAllocatedBytesBegin2 = dto.getMemoryAllocatedBytesBegin();
		this.memoryAllocatedBytesEnded = dto.getMemoryAllocatedBytesEnded();
		this.memoryAllocatedBytesEnded2 = dto.getMemoryAllocatedBytesEnded();
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
