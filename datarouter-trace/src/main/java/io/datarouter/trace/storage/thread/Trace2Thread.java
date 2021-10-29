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
package io.datarouter.trace.storage.thread;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.trace.storage.trace.Trace2;

public class Trace2Thread extends BaseDatabean<Trace2ThreadKey,Trace2Thread>{

	@Deprecated
	private Long parentThreadId;
	private Long parentThreadId2;
	private String name;
	private String info;
	private String serverName;
	@Deprecated
	private Long created;
	private Long created2;
	@Deprecated
	private Long queuedEnded;
	private Long queuedEnded2;
	@Deprecated
	private Long ended;
	private Long ended2;
	@Deprecated
	private Integer discardedSpanCount;
	private Integer discardedSpanCount2;
	private String hostThreadName;
	@Deprecated
	private Integer totalSpanCount;
	private Integer totalSpanCount2;
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
		public static final UInt63FieldKey parentThreadId = new UInt63FieldKey("parentThreadId");
		public static final LongFieldKey parentThreadId2 = new LongFieldKey("parentThreadId2");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		@Deprecated
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final LongFieldKey created2 = new LongFieldKey("created2");
		@Deprecated
		public static final UInt63FieldKey queuedEnded = new UInt63FieldKey("queuedEnded");
		public static final LongFieldKey queuedEnded2 = new LongFieldKey("queuedEnded2");
		@Deprecated
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
		public static final LongFieldKey ended2 = new LongFieldKey("ended2");
		@Deprecated
		public static final UInt31FieldKey discardedSpanCount = new UInt31FieldKey("discardedSpanCount");
		public static final IntegerFieldKey discardedSpanCount2 = new IntegerFieldKey("discardedSpanCount2");
		public static final StringFieldKey hostThreadName = new StringFieldKey("hostThreadName");
		@Deprecated
		public static final UInt31FieldKey totalSpanCount = new UInt31FieldKey("totalSpanCount");
		public static final IntegerFieldKey totalSpanCount2 = new IntegerFieldKey("totalSpanCount2");
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

	public static class Trace2ThreadFielder extends BaseDatabeanFielder<Trace2ThreadKey,Trace2Thread>{

		public Trace2ThreadFielder(){
			super(Trace2ThreadKey::new);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Thread databean){
			return List.of(
					new UInt63Field(FieldKeys.parentThreadId, databean.parentThreadId),
					new LongField(FieldKeys.parentThreadId2, databean.parentThreadId2),
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.info, databean.info),
					new StringField(FieldKeys.serverName, databean.serverName),
					new UInt63Field(FieldKeys.created, databean.created),
					new LongField(FieldKeys.created2, databean.created2),
					new UInt63Field(FieldKeys.queuedEnded, databean.queuedEnded),
					new LongField(FieldKeys.queuedEnded2, databean.queuedEnded2),
					new UInt63Field(FieldKeys.ended, databean.ended),
					new LongField(FieldKeys.ended2, databean.ended2),
					new UInt31Field(FieldKeys.discardedSpanCount, databean.discardedSpanCount),
					new IntegerField(FieldKeys.discardedSpanCount2, databean.discardedSpanCount2),
					new StringField(FieldKeys.hostThreadName, databean.hostThreadName),
					new UInt31Field(FieldKeys.totalSpanCount, databean.totalSpanCount),
					new IntegerField(FieldKeys.totalSpanCount2, databean.totalSpanCount2),
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

	public Trace2Thread(){
		this(new Trace2ThreadKey());
	}

	public Trace2Thread(Trace2ThreadKey key){
		super(key);
	}

	public Trace2Thread(Trace2ThreadDto dto){
		super(new Trace2ThreadKey(dto.traceparent, dto.threadId));
		this.parentThreadId = dto.parentThreadId;
		this.parentThreadId2 = dto.parentThreadId;
		this.name = dto.name;
		this.info = dto.getInfo();
		this.serverName = dto.serverName;
		this.created = dto.created;
		this.created2 = dto.created;
		this.queuedEnded = dto.getQueuedEnded();
		this.queuedEnded2 = dto.getQueuedEnded();
		this.ended = dto.getEnded();
		this.ended2 = dto.getEnded();
		this.discardedSpanCount = dto.getDiscardedSpanCount();
		this.discardedSpanCount2 = dto.getDiscardedSpanCount();
		this.hostThreadName = dto.hostThreadName;
		this.totalSpanCount = dto.getTotalSpanCount();
		this.totalSpanCount2 = dto.getTotalSpanCount();
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
	public Supplier<Trace2ThreadKey> getKeySupplier(){
		return Trace2ThreadKey::new;
	}

	public Date getTime(){
		return new Date(TimeUnit.NANOSECONDS.toMillis(created));
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

	public Long getCreatedNs(){
		return created;
	}

	public Long getQueuedEndedNs(){
		return queuedEnded;
	}

	public Long getEndedNs(){
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

	public Long getDurationNs(){
		return ended - created;
	}

	public Long getQueueDurationNs(){
		return queuedEnded - created;
	}

	@Override
	public String toString(){
		return super.toString() + "[" + name + "]";
	}
}
