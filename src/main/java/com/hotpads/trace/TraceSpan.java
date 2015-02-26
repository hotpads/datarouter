package com.hotpads.trace;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class TraceSpan extends BaseDatabean<TraceSpanKey,TraceSpan>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	@Id
	private TraceSpanKey key;
	private Integer parentSequence;
	@Column(length=DEFAULT_STRING_LENGTH)
	private String name;
	private Long created;
	private Long duration;
	private String info;

	@Transient 
	private Long nanoStart;
	private Long durationNano;
		
	
	public static class Fields{
		public static final String
			key = "key",
			parentSequence = "parentSequence",
			name = "name",
			info = "info",
			created = "created",
			duration = "duration",
			durationNano = "durationNano";
	}
	
	//test implementation.  just repeats the built-in databean fields for now
	public static class TraceSpanFielder extends BaseDatabeanFielder<TraceSpanKey,TraceSpan>{
		public TraceSpanFielder(){super();}
		@Override
		public Class<TraceSpanKey> getKeyFielderClass(){
			return TraceSpanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(TraceSpan d){
			return FieldTool.createList(
					new UInt31Field(Fields.parentSequence, d.parentSequence),
					new StringField(Fields.name, d.name, DEFAULT_STRING_LENGTH),
					new StringField(Fields.info, d.info, DEFAULT_STRING_LENGTH),
					new UInt63Field(Fields.created, d.created),
					new UInt63Field(Fields.duration, d.duration),
					new UInt63Field(Fields.durationNano, d.durationNano));
		}
	}
	
	
	/*********************** constructor **********************************/
	
	TraceSpan(){
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
	public Class<TraceSpanKey> getKeyClass() {
		return TraceSpanKey.class;
	}
	
	@Override
	public TraceSpanKey getKey() {
		return key;
	}
	
	public TraceThreadKey getThreadKey(){
		return new TraceThreadKey(this.getTraceId(), this.getThreadId());
	}
	
	
	/**************************** standard *****************************************/
	
	@Override
	public String toString(){
		return super.toString()+"["+name+"]";
	}

	
	/****************************** static ****************************************/
	
	public static SortedMap<TraceThreadKey,SortedSet<TraceSpan>> getByThreadKey(
			Iterable<TraceSpan> spans){
		SortedMap<TraceThreadKey,SortedSet<TraceSpan>> out = DrMapTool.createTreeMap();
		for(TraceSpan s : DrIterableTool.nullSafe(spans)){
			TraceThreadKey threadKey = s.getThreadKey();
			if(out.get(threadKey)==null){ out.put(threadKey, new TreeSet<TraceSpan>()); }
			out.get(threadKey).add(s);
		}
		return out;
	}
	
	public static Long totalDurationOfNonChildren(Iterable<TraceSpan> spans){
		Long sum = 0L;
		for(TraceSpan s : DrIterableTool.nullSafe(spans)){
			if(s.isTopLevel()){
				sum += DrNumberTool.nullSafeLong(s.getDuration(), 0L);
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
		return this.parentSequence==null;
	}
	
	/********************************* get/set ****************************************/


	public void setKey(TraceSpanKey key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Long getCreated() {
		return created;
	}


	public void setCreated(Long created) {
		this.created = created;
	}



	public Integer getSequence() {
		return key.getSequence();
	}


	public Long getThreadId() {
		return key.getThreadId();
	}

	public Long getTraceId(){
		return key.getTraceId();
	}

	public void setSequence(Integer sequence){
		key.setSequence(sequence);
	}

	public void setThreadId(Long threadId){
		key.setThreadId(threadId);
	}

	public void setTraceId(Long traceId){
		key.setTraceId(traceId);
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

	public void setParentSequence(Integer parentSequence){
		this.parentSequence = parentSequence;
	}


	public String getInfo(){
		return info;
	}


	public void setInfo(String info){
		this.info = info;
	}

	
}
