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
package io.datarouter.storage.trace.databean;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.util.ComparableTool;

public abstract class BaseTraceThread<
		EK extends BaseTraceEntityKey<EK>,
		PK extends BaseTraceThreadKey<EK,PK>,
		D extends BaseTraceThread<EK,PK,D>>
extends BaseDatabean<PK,D>{

	protected PK key;
	protected Long parentId;
	protected String name;
	protected String info;
	protected String serverId;//should be serverName
	protected Long created;
	protected Long queuedDuration;
	protected Long runningDuration;
	protected Long nanoStart;
	protected Long queuedDurationNano;
	protected Long runningDurationNano;

	public static class FieldKeys{
		public static final UInt63FieldKey parentId = new UInt63FieldKey("parentId");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final StringFieldKey serverId = new StringFieldKey("serverId");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey queuedDuration = new UInt63FieldKey("queuedDuration");
		public static final UInt63FieldKey runningDuration = new UInt63FieldKey("runningDuration");
		public static final UInt63FieldKey queuedDurationNano = new UInt63FieldKey("queuedDurationNano");
		public static final UInt63FieldKey runningDurationNano = new UInt63FieldKey("runningDurationNano");
	}

	public static class BaseTraceThreadFielder<
			EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceThreadKey<EK,PK>,
			D extends BaseTraceThread<EK,PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseTraceThreadFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D traceThread){
			return Arrays.asList(
					new UInt63Field(FieldKeys.parentId, traceThread.getParentId()),
					new StringField(FieldKeys.name, traceThread.getName()),
					new StringField(FieldKeys.info, traceThread.getInfo()),
					new StringField(FieldKeys.serverId, traceThread.getServerId()),
					new UInt63Field(FieldKeys.created, traceThread.getCreated()),
					new UInt63Field(FieldKeys.queuedDuration, traceThread.getQueuedDuration()),
					new UInt63Field(FieldKeys.runningDuration, traceThread.getRunningDuration()),
					new UInt63Field(FieldKeys.queuedDurationNano, traceThread.getQueuedDurationNano()),
					new UInt63Field(FieldKeys.runningDurationNano, traceThread.getRunningDurationNano()));
		}

	}

	/*------------------------------ construct ------------------------------*/

	public BaseTraceThread(){
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}

	public BaseTraceThread(TraceThreadDto dto){
		this.parentId = dto.parentId;
		this.name = dto.name;
		this.info = dto.info;
		this.serverId = dto.serverId;
		this.created = dto.created;
		this.queuedDuration = dto.queuedDuration;
		this.runningDuration = dto.runningDuration;
		this.queuedDurationNano = dto.queuedDurationNano;
		this.runningDurationNano = dto.runningDurationNano;
	}

	/*------------------------------- compare -------------------------------*/

	//not sure this is useful, but TraceThreadGroupComparator extends it
	public static class TraceThreadComparatorBaseTraceThread<
			EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceThreadKey<EK,PK>,
			D extends BaseTraceThread<EK,PK,D>>
	implements Comparator<D>{

		@Override
		public int compare(D threadA, D threadB){
			int d0 = ComparableTool.nullFirstCompareTo(threadA.getParentId(), threadB.getParentId());
			if(d0 != 0){
				return d0;
			}
			int d1 = ComparableTool.nullFirstCompareTo(threadA.getCreated(), threadB.getCreated());
			if(d1 != 0){
				return d1;
			}
			return ComparableTool.nullFirstCompareTo(threadA.getName(), threadB.getName());
		}

	}

	/*------------------------------- methods -------------------------------*/

	@Override
	public String toString(){
		return super.toString() + "[" + name + "]";
	}

	public void markStart(){
		queuedDuration = System.currentTimeMillis() - created;
		queuedDurationNano = System.nanoTime() - nanoStart;
	}

	public void markFinish(){
		runningDuration = System.currentTimeMillis() - queuedDuration - created;
		runningDurationNano = System.nanoTime() - queuedDurationNano - nanoStart;
	}

	public Date getTime(){
		return new Date(created);
	}

	public Long getTotalDuration(){
		return getQueuedDuration() + getRunningDuration();
	}

	/*------------------------------- get/set -------------------------------*/

	@Override
	public PK getKey(){
		return key;
	}

	// should be serverName
	public String getServerId(){
		return serverId;
	}

	public void setServerId(String serverId){
		this.serverId = serverId;
	}

	public Long getParentId(){
		return parentId;
	}

	public void setParentId(Long parentId){
		this.parentId = parentId;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public Long getCreated(){
		return created;
	}

	public void setCreated(Long created){
		this.created = created;
	}

	public Long getTraceId(){
		return key.getTraceId();
	}

	public Long getQueuedDuration(){
		return queuedDuration;
	}

	public void setQueuedDuration(Long queuedDuration){
		this.queuedDuration = queuedDuration;
	}

	public Long getRunningDuration(){
		return runningDuration;
	}

	public void setRunningDuration(Long runningDuration){
		this.runningDuration = runningDuration;
	}

	public Long getQueuedDurationNano(){
		return queuedDurationNano;
	}

	public void setQueuedDurationNano(Long queuedDurationNano){
		this.queuedDurationNano = queuedDurationNano;
	}

	public Long getRunningDurationNano(){
		return runningDurationNano;
	}

	public void setRunningDurationNano(Long runningDurationNano){
		this.runningDurationNano = runningDurationNano;
	}

	public Long getNanoStart(){
		return nanoStart;
	}

	public void setNanoStart(Long nanoStart){
		this.nanoStart = nanoStart;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

}
