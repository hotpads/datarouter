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
		public static final LongFieldKey parentThreadId = new LongFieldKey("parentThreadId")
				.withColumnName("parentThreadId2");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final LongFieldKey created = new LongFieldKey("created")
				.withColumnName("created2");
		public static final LongFieldKey queuedEnded = new LongFieldKey("queuedEnded")
				.withColumnName("queuedEnded2");
		public static final LongFieldKey ended = new LongFieldKey("ended")
				.withColumnName("ended2");
		public static final IntegerFieldKey discardedSpanCount = new IntegerFieldKey("discardedSpanCount")
				.withColumnName("discardedSpanCount2");
		public static final StringFieldKey hostThreadName = new StringFieldKey("hostThreadName");
		public static final IntegerFieldKey totalSpanCount = new IntegerFieldKey("totalSpanCount")
				.withColumnName("totalSpanCount2");
		public static final LongFieldKey cpuTimeCreatedNs = new LongFieldKey("cpuTimeCreatedNs")
				.withColumnName("cpuTimeCreatedNs2");
		public static final LongFieldKey cpuTimeEndedNs = new LongFieldKey("cpuTimeEndedNs")
				.withColumnName("cpuTimeEndedNs2");
		public static final LongFieldKey memoryAllocatedBytesBegin = new LongFieldKey("memoryAllocatedBytesBegin")
				.withColumnName("memoryAllocatedBytesBegin2");
		public static final LongFieldKey memoryAllocatedBytesEnded = new LongFieldKey("memoryAllocatedBytesEnded")
				.withColumnName("memoryAllocatedBytesEnded2");
	}

	public static class Trace2ThreadFielder extends BaseDatabeanFielder<Trace2ThreadKey,Trace2Thread>{

		public Trace2ThreadFielder(){
			super(Trace2ThreadKey::new);
			addOption(Trace2.TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2Thread databean){
			return List.of(
					new LongField(FieldKeys.parentThreadId, databean.parentThreadId),
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.info, databean.info),
					new StringField(FieldKeys.serverName, databean.serverName),
					new LongField(FieldKeys.created, databean.created),
					new LongField(FieldKeys.queuedEnded, databean.queuedEnded),
					new LongField(FieldKeys.ended, databean.ended),
					new IntegerField(FieldKeys.discardedSpanCount, databean.discardedSpanCount),
					new StringField(FieldKeys.hostThreadName, databean.hostThreadName),
					new IntegerField(FieldKeys.totalSpanCount, databean.totalSpanCount),
					new LongField(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new LongField(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new LongField(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new LongField(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded));
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

	public Long getCreated(){
		return created;
	}

	public Long getQueuedEnded(){
		return queuedEnded;
	}

	public Long getEnded(){
		return ended;
	}

	public Long getCpuTimeCreatedNs(){
		return cpuTimeCreatedNs;
	}

	public Long getCpuTimeEndedNs(){
		return cpuTimeEndedNs;
	}

	public Long getMemoryAllocatedBytesBegin(){
		return memoryAllocatedBytesBegin;
	}

	public Long getMemoryAllocatedBytesEnded(){
		return memoryAllocatedBytesEnded;
	}

	@Override
	public String toString(){
		return super.toString() + "[" + name + "]";
	}
}
