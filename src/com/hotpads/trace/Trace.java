package com.hotpads.trace;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.StringTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class Trace extends BaseDatabean<TraceKey>{

	@Id
	protected TraceKey key;
	
	@Column(length=32)
	protected String sessionId;
	@Column(length=20)
	protected String context;
	@Column(length=255)
	protected String type;
	@Column(length=255)
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
	
	public static final String
		KEY_key = "key",
		COL_sessionId = "sessionId",
		COL_context = "context",
		COL_type = "type",
		COL_params = "params",
		COL_created = "created",
		COL_duration = "duration";

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringField(COL_sessionId, this.sessionId),
				new StringField(COL_context, this.context),
				new StringField(COL_type, this.type),
				new StringField(COL_params, this.params),
				new UInt63Field(COL_created, this.created),
				new UInt63Field(COL_duration, this.duration));
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
		this.duration = System.currentTimeMillis() - this.created;
		this.durationNano = System.nanoTime() - this.nanoStart;
	}
	
	public String getRequestString(){
		return this.context+"/"+this.type+"?"+StringTool.nullSafe(this.params);
	}
	
	public Date getTime(){
		return new Date(this.created);
	}
	
	public Long getMsSinceCreated(){
		return System.currentTimeMillis() - this.created;
	}
	
	/******************** validate *****************************************/
	
	public void trimStringsToFit(){
		if(StringTool.exceedsLength(this.sessionId, LEN_sessionId)){
			this.sessionId = this.sessionId.substring(0, LEN_sessionId);
		}
		if(StringTool.exceedsLength(this.context, LEN_context)){
			this.context = this.context.substring(0, LEN_context);
		}
		if(StringTool.exceedsLength(this.type, LEN_type)){
			this.type = this.type.substring(0, LEN_type);
		}
		if(StringTool.exceedsLength(this.params, LEN_params)){
			this.params = this.params.substring(0, LEN_params);
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
