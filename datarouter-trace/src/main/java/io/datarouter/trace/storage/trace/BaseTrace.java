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

import java.util.Arrays;
import java.util.List;

import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.trace.storage.entity.BaseTraceEntityKey;
import io.datarouter.util.string.StringTool;

public abstract class BaseTrace<
		EK extends BaseTraceEntityKey<EK>,
		PK extends BaseTraceKey<EK,PK>,
		D extends BaseTrace<EK,PK,D>>
extends BaseDatabean<PK,D>{

	protected String context;
	protected String type;
	protected String params;
	protected Long created;
	protected Long duration;
	protected Integer discardedThreadCount;

	public static class FieldKeys{
		public static final StringFieldKey context = new StringFieldKey("context");
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey params = new StringFieldKey("params");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
		public static final UInt31FieldKey discardedThreadCount = new UInt31FieldKey("discardedThreadCount");
	}

	public static class BaseTraceFielder<
			EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceKey<EK,PK>,
			D extends BaseTrace<EK,PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseTraceFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return Arrays.asList(
					new StringField(FieldKeys.context, databean.getContext()),
					new StringField(FieldKeys.type, databean.getType()),
					new StringField(FieldKeys.params, databean.getParams()),
					new UInt63Field(FieldKeys.created, databean.getCreated()),
					new UInt63Field(FieldKeys.duration, databean.getDuration()),
					new UInt31Field(FieldKeys.discardedThreadCount, databean.getDiscardedThreadCount()));
		}

	}

	/*------------------------------ construct ------------------------------*/

	public BaseTrace(PK key){
		super(key);
	}

	public BaseTrace(PK key, TraceDto dto){
		super(key);
		this.context = StringTool.trimToSize(dto.getContext(), FieldKeys.context.getSize());
		this.type = StringTool.trimToSize(dto.getType(), FieldKeys.type.getSize());
		this.params = StringTool.trimToSize(dto.getParams(), FieldKeys.params.getSize());
		this.created = dto.getCreated();
		this.duration = dto.getDuration();
		this.discardedThreadCount = dto.getDiscardedThreadCount();
	}

	public TraceDto toDto(){
		return new TraceDto(
				getTraceId(),
				getContext(),
				getType(),
				getParams(),
				getCreated(),
				getDuration(),
				getDiscardedThreadCount());
	}

	/*------------------------------- get/set -------------------------------*/

	public String getTraceId(){
		return getKey().getEntityKey().getTraceEntityId();
	}

	public String getParams(){
		return params;
	}

	public void setParams(String params){
		this.params = params;
	}

	public String getContext(){
		return context;
	}

	public void setContext(String context){
		this.context = context;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public Long getCreated(){
		return created;
	}

	public void setCreated(Long created){
		this.created = created;
	}

	public Long getDuration(){
		return duration;
	}

	public void setDuration(Long duration){
		this.duration = duration;
	}

	public Integer getDiscardedThreadCount(){
		return discardedThreadCount;
	}

	public void setDiscardedThreadCount(Integer discardedThreadCount){
		this.discardedThreadCount = discardedThreadCount;
	}

}
