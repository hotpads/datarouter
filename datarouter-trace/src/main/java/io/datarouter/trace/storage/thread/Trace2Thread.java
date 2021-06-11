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
package io.datarouter.trace.storage.thread;

import java.util.Date;
import java.util.List;

import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.trace.storage.trace.Trace2;

public class Trace2Thread extends BaseDatabean<Trace2ThreadKey,Trace2Thread>{

	private Long parentThreadId;
	private String name;
	private String info;
	private String serverName;
	private Long created;
	private Long queuedEnded;
	private Long ended;
	private Integer discardedSpanCount;
	private String hostThreadName;
	private Integer totalSpanCount;
	private Long cpuTimeCreatedNs;
	private Long cpuTimeEndedNs;
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesEnded;

	public static class FieldKeys{
		public static final UInt63FieldKey parentThreadId = new UInt63FieldKey("parentThreadId");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey queuedEnded = new UInt63FieldKey("queuedEnded");
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
		public static final UInt31FieldKey discardedSpanCount = new UInt31FieldKey("discardedSpanCount");
		public static final StringFieldKey hostThreadName = new StringFieldKey("hostThreadName");
		public static final UInt31FieldKey totalSpanCount = new UInt31FieldKey("totalSpanCount");
		public static final UInt63FieldKey cpuTimeCreatedNs = new UInt63FieldKey("cpuTimeCreatedNs");
		public static final UInt63FieldKey cpuTimeEndedNs = new UInt63FieldKey("cpuTimeEndedNs");
		public static final UInt63FieldKey memoryAllocatedBytesBegin = new UInt63FieldKey("memoryAllocatedBytesBegin");
		public static final UInt63FieldKey memoryAllocatedBytesEnded = new UInt63FieldKey("memoryAllocatedBytesEnded");
	}

	public static class Trace2ThreadFielder extends BaseDatabeanFielder<Trace2ThreadKey,Trace2Thread>{

		public Trace2ThreadFielder(){
			super(Trace2ThreadKey.class);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Thread databean){
			return List.of(
					new UInt63Field(FieldKeys.parentThreadId, databean.parentThreadId),
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.info, databean.info),
					new StringField(FieldKeys.serverName, databean.serverName),
					new UInt63Field(FieldKeys.created, databean.created),
					new UInt63Field(FieldKeys.queuedEnded, databean.queuedEnded),
					new UInt63Field(FieldKeys.ended, databean.ended),
					new UInt31Field(FieldKeys.discardedSpanCount, databean.discardedSpanCount),
					new StringField(FieldKeys.hostThreadName, databean.hostThreadName),
					new UInt31Field(FieldKeys.totalSpanCount, databean.totalSpanCount),
					new UInt63Field(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new UInt63Field(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new UInt63Field(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new UInt63Field(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded));
		}

	}

	public Trace2Thread(){
		this(new Trace2ThreadKey());
	}

	public Trace2Thread(Trace2ThreadKey key){
		super(key);
	}

	public Trace2Thread(Trace2ThreadDto dto){
		super(new Trace2ThreadKey(dto.traceparent, dto.threadId));
		this.parentThreadId = dto.parentThreadId;
		this.name = dto.name;
		this.info = dto.getInfo();
		this.serverName = dto.serverName;
		this.created = dto.created;
		this.queuedEnded = dto.getQueuedEnded();
		this.ended = dto.getEnded();
		this.discardedSpanCount = dto.getDiscardedSpanCount();
		this.hostThreadName = dto.hostThreadName;
		this.totalSpanCount = dto.getTotalSpanCount();
		this.cpuTimeCreatedNs = dto.getCpuTimeCreatedNs();
		this.cpuTimeEndedNs = dto.getCpuTimeEndedNs();
		this.memoryAllocatedBytesBegin = dto.getMemoryAllocatedBytesBegin();
		this.memoryAllocatedBytesEnded = dto.getMemoryAllocatedBytesEnded();
	}

	@Override
	public Class<Trace2ThreadKey> getKeyClass(){
		return Trace2ThreadKey.class;
	}

	public Date getTime(){
		return new Date(created);
	}

	public Long getThreadId(){
		return getKey().getThreadId();
	}

	public String getServerName(){
		return serverName;
	}

	public Long getParentThreadId(){
		return parentThreadId;
	}

	public String getName(){
		return name;
	}

	public Long getCreated(){
		return created;
	}

	public Long getQueuedEnded(){
		return queuedEnded;
	}

	public Long getEnded(){
		return ended;
	}

	public Integer getDiscardedSpanCount(){
		return discardedSpanCount;
	}

	public String getInfo(){
		return info;
	}

	public String getHostThreadName(){
		return hostThreadName;
	}

	public Integer getTotalSpanCount(){
		return totalSpanCount;
	}

	public Long getDuration(){
		return ended - created;
	}

	@Override
	public String toString(){
		return super.toString() + "[" + name + "]";
	}
}
