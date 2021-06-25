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

import java.util.List;

import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
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
		public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
		public static final StringFieldKey name = new StringFieldKey("name")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEnumFieldKey<Trace2SpanGroupType> groupType = new StringEnumFieldKey<>("groupType",
				Trace2SpanGroupType.class);
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
		public static final UInt63FieldKey cpuTimeCreatedNs = new UInt63FieldKey("cpuTimeCreatedNs");
		public static final UInt63FieldKey cpuTimeEndedNs = new UInt63FieldKey("cpuTimeEndedNs");
		public static final UInt63FieldKey memoryAllocatedBytesBegin = new UInt63FieldKey("memoryAllocatedBytesBegin");
		public static final UInt63FieldKey memoryAllocatedBytesEnded = new UInt63FieldKey("memoryAllocatedBytesEnded");
	}

	public static class Trace2SpanFielder extends BaseDatabeanFielder<Trace2SpanKey,Trace2Span>{

		public Trace2SpanFielder(){
			super(Trace2SpanKey.class);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Span databean){
			return List.of(
					new UInt31Field(FieldKeys.parentSequence, databean.parentSequence),
					new StringField(FieldKeys.name, databean.name),
					new StringEnumField<>(FieldKeys.groupType, databean.groupType),
					new StringField(FieldKeys.info, databean.info),
					new UInt63Field(FieldKeys.created, databean.created),
					new UInt63Field(FieldKeys.ended, databean.ended),
					new UInt63Field(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new UInt63Field(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new UInt63Field(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new UInt63Field(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded));
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
		this.groupType = dto.groupType != null ? Trace2SpanGroupType.fromPersistentStringStatic(dto.groupType.type)
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
	public Class<Trace2SpanKey> getKeyClass(){
		return Trace2SpanKey.class;
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

	public Long getCreated(){
		return created;
	}

	public Long getEnded(){
		return ended;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public String getInfo(){
		return info;
	}

	public Long getDuration(){
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
