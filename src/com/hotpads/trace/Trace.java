package com.hotpads.trace;

import java.util.Date;
import java.util.List;

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
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.StringTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class Trace extends BaseDatabean<TraceKey,Trace>{

	public static final int DEFAULT_STRING_VALUE = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	@Id
	protected TraceKey key;
	
	@Column(length=32)
	protected String sessionId;
	@Column(length=20)
	protected String context;
	@Column(length=DEFAULT_STRING_VALUE)
	protected String type;
	@Column(length=DEFAULT_STRING_VALUE)
	protected String params;
	
	protected Long created;
	protected Long duration;
	
	@Transient
	protected Long nanoStart;
	protected Long durationNano;
		
	
	/**************************** columns *******************************/
	
	public static final Integer
		LEN_instanceId = 20,
		LEN_sessionId = 32,
		LEN_context = 20,
		LEN_type = 255,
		LEN_params = 255;
	
	public static class F{
		public static final String
			KEY_key = "key",
			sessionId = "sessionId",
			context = "context",
			type = "type",
			params = "params",
			created = "created",
			duration = "duration";
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringField(F.sessionId, sessionId,LEN_sessionId),
				new StringField(F.context, context,LEN_context),
				new StringField(F.type, type, DEFAULT_STRING_VALUE),
				new StringField(F.params, params, DEFAULT_STRING_VALUE),
				new UInt63Field(F.created, created),
				new UInt63Field(F.duration, duration));
	}
	
	public static class TraceFielder extends BaseDatabeanFielder<TraceKey,Trace>{
		public TraceFielder(){}
		@Override
		public Class<TraceKey> getKeyFielderClass(){
			return TraceKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(Trace d){
			return d.getNonKeyFields();
		}
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	/*********************** constructor **********************************/
	
	public Trace(){
		this.key = new TraceKey();
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public Class<TraceKey> getKeyClass() {
		return TraceKey.class;
	};
	
	@Override
	public TraceKey getKey() {
		return key;
	}
	
	
	/******************** static ******************************************/
	
	public static void trimStringsToFit(Iterable<Trace> traces){
		for(Trace trace : IterableTool.nullSafe(traces)){
			if(trace==null){ continue; }
			trace.trimStringsToFit();
		}
	}
	
	/******************** methods ********************************************/
	
	public void markFinished(){
		duration = System.currentTimeMillis() - created;
		durationNano = System.nanoTime() - nanoStart;
	}
	
	public String getRequestString(){
		return StringTool.nullSafe(context)+"/"+type+"?"+StringTool.nullSafe(params);
	}
	
	public Date getTime(){
		return new Date(created);
	}
	
	public Long getMsSinceCreated(){
		return System.currentTimeMillis() - created;
	}
	
	/******************** validate *****************************************/
	
	public void trimStringsToFit(){
		if(StringTool.exceedsLength(sessionId, LEN_sessionId)){
			sessionId = sessionId.substring(0, LEN_sessionId);
		}
		if(StringTool.exceedsLength(context, LEN_context)){
			context = context.substring(0, LEN_context);
		}
		if(StringTool.exceedsLength(type, LEN_type)){
			type = type.substring(0, LEN_type);
		}
		if(StringTool.exceedsLength(params, LEN_params)){
			params = params.substring(0, LEN_params);
		}
	}

	
	/********************************* get/set ****************************************/


	public void setKey(TraceKey key) {
		this.key = key;
	}

	public Long getId() {
		return key.getId();
	}


	public void setId(Long id) {
		key.setId(id);
	}


	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public Long getCreated() {
		return created;
	}


	public void setCreated(Long created) {
		this.created = created;
	}


	public Long getDuration() {
		return duration;
	}


	public void setDuration(Long duration) {
		this.duration = duration;
	}


	public Long getDurationNano() {
		return durationNano;
	}


	public void setDurationNano(Long durationNano) {
		this.durationNano = durationNano;
	}

	
}
