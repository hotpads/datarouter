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
package io.datarouter.trace.storage.trace;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.TtlFielderConfig;

public class Trace2 extends BaseDatabean<Trace2Key,Trace2>{

	public static final Duration TTL = Duration.ofDays(15);
	public static final TtlFielderConfig TTL_FIELDER_CONFIG = new TtlFielderConfig(TTL);
	public static final String DEFAULT_ACCOUNT_NAME = "default";

	private String initialParentId;
	private String context;
	private String type;
	private String params;
	private Long created;
	private Long ended;
	private String accountName; // multiple serviceNames could be tied to one accountName
	private String serviceName;
	private Integer discardedThreadCount;
	private Integer totalThreadCount;
	private Long cpuTimeCreatedNs;
	private Long cpuTimeEndedNs;
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesEnded;

	public static class FieldKeys{
		public static final StringFieldKey initialParentId = new StringFieldKey("initialParentId");
		public static final StringFieldKey context = new StringFieldKey("context");
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey params = new StringFieldKey("params");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey ended = new UInt63FieldKey("ended");
		public static final StringFieldKey accountName = new StringFieldKey("accountName");
		public static final StringFieldKey serviceName = new StringFieldKey("serviceName");
		public static final UInt31FieldKey discardedThreadCount = new UInt31FieldKey("discardedThreadCount");
		public static final UInt31FieldKey totalThreadCount = new UInt31FieldKey("totalThreadCount");
		public static final UInt63FieldKey cpuTimeCreatedNs = new UInt63FieldKey("cpuTimeCreatedNs");
		public static final UInt63FieldKey cpuTimeEndedNs = new UInt63FieldKey("cpuTimeEndedNs");
		public static final UInt63FieldKey memoryAllocatedBytesBegin = new UInt63FieldKey("memoryAllocatedBytesBegin");
		public static final UInt63FieldKey memoryAllocatedBytesEnded = new UInt63FieldKey("memoryAllocatedBytesEnded");
	}

	public static class Trace2Fielder extends BaseDatabeanFielder<Trace2Key,Trace2>{

		public Trace2Fielder(){
			super(Trace2Key.class);
			addOption(TTL_FIELDER_CONFIG);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Trace2 databean){
			return List.of(
					new StringField(FieldKeys.initialParentId, databean.initialParentId),
					new StringField(FieldKeys.context, databean.context),
					new StringField(FieldKeys.type, databean.type),
					new StringField(FieldKeys.params, databean.params),
					new UInt63Field(FieldKeys.created, databean.created),
					new UInt63Field(FieldKeys.ended, databean.ended),
					new StringField(FieldKeys.accountName, databean.accountName),
					new StringField(FieldKeys.serviceName, databean.serviceName),
					new UInt31Field(FieldKeys.discardedThreadCount, databean.discardedThreadCount),
					new UInt31Field(FieldKeys.totalThreadCount, databean.totalThreadCount),
					new UInt63Field(FieldKeys.cpuTimeCreatedNs, databean.cpuTimeCreatedNs),
					new UInt63Field(FieldKeys.cpuTimeEndedNs, databean.cpuTimeEndedNs),
					new UInt63Field(FieldKeys.memoryAllocatedBytesBegin, databean.memoryAllocatedBytesBegin),
					new UInt63Field(FieldKeys.memoryAllocatedBytesEnded, databean.memoryAllocatedBytesEnded));
		}
	}

	public Trace2(){
		this(new Trace2Key());
	}

	public Trace2(Trace2Key key){
		super(key);
	}

	public Trace2(String accountName, Trace2Dto dto){
		super(new Trace2Key(dto.traceparent));
		this.initialParentId = dto.initialParentId;
		this.context = dto.context;
		this.type = dto.type;
		this.params = dto.params;
		this.created = dto.created;
		this.ended = dto.ended;
		this.serviceName = dto.serviceName;
		this.accountName = accountName;
		this.discardedThreadCount = dto.discardedThreadCount;
		this.totalThreadCount = dto.totalThreadCount;
		this.cpuTimeCreatedNs = dto.cpuTimeCreatedNs;
		this.cpuTimeEndedNs = dto.cpuTimeEndedNs;
		this.memoryAllocatedBytesBegin = dto.memoryAllocatedBytesBegin;
		this.memoryAllocatedBytesEnded = dto.memoryAllocatedBytesEnded;
	}

	@Override
	public Supplier<Trace2Key> getKeySupplier(){
		return Trace2Key::new;
	}

	public String getTraceId(){
		return getKey().getEntityKey().getTrace2EntityId();
	}

	public String getParentId(){
		return getKey().getParentId();
	}

	public String getInitialParentId(){
		return initialParentId;
	}

	public String getParams(){
		return params;
	}

	public String getContext(){
		return context;
	}

	public String getType(){
		return type;
	}

	public Long getCreatedNs(){
		return created;
	}

	public Long getCreatedMs(){
		return TimeUnit.NANOSECONDS.toMillis(created);
	}

	public Long getEndedNs(){
		return ended;
	}

	public String getServiceName(){
		return serviceName;
	}

	public String getAccountName(){
		return accountName;
	}

	public Integer getDiscardedThreadCount(){
		return discardedThreadCount;
	}

	public Integer getTotalThreadCount(){
		return totalThreadCount;
	}

	public Long getDurationNs(){
		return ended - created;
	}

	public Long getCpuTimeUsedNs(){
		return cpuTimeEndedNs != null && cpuTimeCreatedNs != null ? cpuTimeEndedNs - cpuTimeCreatedNs : null;
	}

	public Long getMemoryAllocatedBytes(){
		return memoryAllocatedBytesEnded != null && memoryAllocatedBytesBegin != null ? memoryAllocatedBytesEnded
				- memoryAllocatedBytesBegin : null;
	}

	public Traceparent getTraceparent(){
		return new Traceparent(getTraceId(), getParentId());
	}

}
