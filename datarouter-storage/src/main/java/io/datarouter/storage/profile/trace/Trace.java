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
package io.datarouter.storage.profile.trace;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.profile.trace.key.TraceKey;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.string.StringTool;

public class Trace extends BaseDatabean<TraceKey,Trace>{

	private TraceKey key;

	private String context;
	private String type;
	private String params;

	private Long created;
	private Long duration;

	private Long nanoStart;
	private Long durationNano;


	/**************************** columns *******************************/

	public static class FieldKeys{
		public static final StringFieldKey context = new StringFieldKey("context").withSize(20);
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final StringFieldKey params = new StringFieldKey("params");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
	}

	public static class TraceFielder extends BaseDatabeanFielder<TraceKey,Trace>{
		public TraceFielder(){
			super(TraceKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(Trace trace){
			return Arrays.asList(
					new StringField(FieldKeys.context, trace.context),
					new StringField(FieldKeys.type, trace.type),
					new StringField(FieldKeys.params, trace.params),
					new UInt63Field(FieldKeys.created, trace.created),
					new UInt63Field(FieldKeys.duration, trace.duration));
		}
	}

	/*********************** constructor **********************************/

	public Trace(){
		this.key = new TraceKey();
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}


	/************************** databean **************************************/

	@Override
	public Class<TraceKey> getKeyClass(){
		return TraceKey.class;
	}

	@Override
	public TraceKey getKey(){
		return key;
	}


	/******************** static ******************************************/

	public static void trimStringsToFitStatic(Iterable<Trace> traces){
		for(Trace trace : IterableTool.nullSafe(traces)){
			if(trace == null){
				continue;
			}
			trace.trimStringsToFit();
		}
	}

	/******************** methods ********************************************/

	public void markFinished(){
		duration = System.currentTimeMillis() - created;
		durationNano = System.nanoTime() - nanoStart;
	}

	public String getRequestString(){
		return StringTool.nullSafe(context) + "/" + type + "?" + StringTool.nullSafe(params);
	}

	public Date getTime(){
		return new Date(created);
	}

	public Long getMsSinceCreated(){
		return System.currentTimeMillis() - created;
	}

	/******************** validate *****************************************/

	public void trimStringsToFit(){
		if(StringTool.exceedsLength(context, FieldKeys.context.getSize())){
			context = context.substring(0, FieldKeys.context.getSize());
		}
		if(StringTool.exceedsLength(type, FieldKeys.type.getSize())){
			type = type.substring(0, FieldKeys.type.getSize());
		}
		if(StringTool.exceedsLength(params, FieldKeys.params.getSize())){
			params = params.substring(0, FieldKeys.params.getSize());
		}
	}


	/********************************* get/set ****************************************/

	public void setKey(TraceKey key){
		this.key = key;
	}

	public Long getId(){
		return key.getId();
	}

	public void setId(Long id){
		key.setId(id);
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

	public Long getDurationNano(){
		return durationNano;
	}

	public void setDurationNano(Long durationNano){
		this.durationNano = durationNano;
	}

}
