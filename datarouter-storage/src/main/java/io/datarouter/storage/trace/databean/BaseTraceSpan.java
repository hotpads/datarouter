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
import java.util.List;

import io.datarouter.instrumentation.trace.TraceSpanDto;
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
import io.datarouter.model.util.CommonFieldSizes;

public abstract class BaseTraceSpan<
		EK extends BaseTraceEntityKey<EK>,
		PK extends BaseTraceSpanKey<EK,PK>,
		TK extends BaseTraceThreadKey<EK,TK>,
		D extends BaseTraceSpan<EK,PK,TK,D>>
extends BaseDatabean<PK,D>{

	protected PK key;

	protected Integer parentSequence;
	protected String name;
	protected Long created;
	protected Long duration;
	protected String info;
	protected Long nanoStart;

	public static class FieldKeys{
		public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
		public static final StringFieldKey name = new StringFieldKey("name")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
	}

	public static class BaseTraceSpanFielder<
			EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceSpanKey<EK,PK>,
			TK extends BaseTraceThreadKey<EK,TK>,
			D extends BaseTraceSpan<EK,PK,TK,D>>
	extends BaseDatabeanFielder<PK,D>{

		protected BaseTraceSpanFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D traceSpan){
			return Arrays.asList(
					new UInt31Field(FieldKeys.parentSequence, traceSpan.getParentSequence()),
					new StringField(FieldKeys.name, traceSpan.getName()),
					new StringField(FieldKeys.info, traceSpan.getInfo()),
					new UInt63Field(FieldKeys.created, traceSpan.getCreated()),
					new UInt63Field(FieldKeys.duration, traceSpan.getDuration()));
		}
	}

	/*------------------------------ construct ------------------------------*/

	public BaseTraceSpan(){
	}

	public BaseTraceSpan(TraceSpanDto dto){
		this.parentSequence = dto.getParentSequence();
		this.name = dto.getName();
		this.info = dto.getInfo();
		this.created = dto.getCreated();
		this.duration = dto.getDuration();
	}

	/*------------------------------- methods -------------------------------*/

	@Override
	public String toString(){
		return key + "[" + name + "][" + info + "]";
	}

	public boolean isTopLevel(){
		return this.parentSequence == null;
	}

	/*------------------------------- get/set -------------------------------*/

	@Override
	public PK getKey(){
		return key;
	}

	public void setKey(PK key){
		this.key = key;
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

	public Integer getSequence(){
		return key.getSequence();
	}

	public Long getThreadId(){
		return key.getThreadId();
	}

	public String getTraceId(){
		return key.getEntityKey().getTraceEntityId();
	}

	public Long getDuration(){
		return duration;
	}

	public void setDuration(Long duration){
		this.duration = duration;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

	public abstract TK getThreadKey();

}