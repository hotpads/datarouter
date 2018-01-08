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
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.profile.trace.key.TraceSpanKey;
import io.datarouter.storage.profile.trace.key.TraceThreadKey;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberTool;

public class TraceSpan extends BaseDatabean<TraceSpanKey,TraceSpan>{

	private TraceSpanKey key;
	private Integer parentSequence;
	private String name;
	private Long created;
	private Long duration;
	private String info;

	private Long nanoStart;
	private Long durationNano;


	public static class FieldKeys{
		public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
		public static final UInt63FieldKey durationNano = new UInt63FieldKey("durationNano");
	}

	//test implementation.  just repeats the built-in databean fields for now
	public static class TraceSpanFielder extends BaseDatabeanFielder<TraceSpanKey,TraceSpan>{
		public TraceSpanFielder(){
			super(TraceSpanKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(TraceSpan traceSpan){
			return Arrays.asList(
					new UInt31Field(FieldKeys.parentSequence, traceSpan.parentSequence),
					new StringField(FieldKeys.name, traceSpan.name),
					new StringField(FieldKeys.info, traceSpan.info),
					new UInt63Field(FieldKeys.created, traceSpan.created),
					new UInt63Field(FieldKeys.duration, traceSpan.duration),
					new UInt63Field(FieldKeys.durationNano, traceSpan.durationNano));
		}
	}


	/*********************** constructor **********************************/

	public TraceSpan(){
		this.key = new TraceSpanKey(null, null, null);
	}

	public TraceSpan(Long traceId, Long threadId, Integer sequence, Integer parentSequence){
		this.key = new TraceSpanKey(traceId, threadId, sequence);
		this.parentSequence = parentSequence;
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}


	/************************** databean **************************************/

	@Override
	public Class<TraceSpanKey> getKeyClass(){
		return TraceSpanKey.class;
	}

	@Override
	public TraceSpanKey getKey(){
		return key;
	}

	public TraceThreadKey getThreadKey(){
		return new TraceThreadKey(this.getTraceId(), this.getThreadId());
	}


	/**************************** standard *****************************************/

	@Override
	public String toString(){
		return key + "[" + name + "][" + info + "]";
	}


	/****************************** static ****************************************/

	public static SortedMap<TraceThreadKey,SortedSet<TraceSpan>> getByThreadKey(Iterable<TraceSpan> spans){
		SortedMap<TraceThreadKey,SortedSet<TraceSpan>> out = new TreeMap<>();
		for(TraceSpan s : IterableTool.nullSafe(spans)){
			TraceThreadKey threadKey = s.getThreadKey();
			if(out.get(threadKey) == null){
				out.put(threadKey, new TreeSet<TraceSpan>());
			}
			out.get(threadKey).add(s);
		}
		return out;
	}

	public static Long totalDurationOfNonChildren(Iterable<TraceSpan> spans){
		Long sum = 0L;
		for(TraceSpan s : IterableTool.nullSafe(spans)){
			if(s.isTopLevel()){
				sum += NumberTool.nullSafeLong(s.getDuration(), 0L);
			}
		}
		return sum;
	}

	/******************************** methods *************************************/

	public void markFinish(){
		this.duration = System.currentTimeMillis() - this.created;
		this.durationNano = System.nanoTime() - this.nanoStart;
	}

	public boolean isTopLevel(){
		return this.parentSequence == null;
	}

	/********************************* get/set ****************************************/

	public void setKey(TraceSpanKey key){
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

	public Long getTraceId(){
		return key.getTraceId();
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

	public Integer getParentSequence(){
		return parentSequence;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

}
