package com.hotpads.trace;

import java.util.Comparator;
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
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.util.core.ComparableTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class TraceThread extends BaseDatabean<TraceThreadKey>{

	@Id
	protected TraceThreadKey key;

	protected Long parentId;
	@Column(length=255)
	protected String name;
	protected String info;
	@Column(length=20)
	protected String serverId;
	protected Long created;
	protected Long queuedDuration;
	protected Long runningDuration;
	
	@Transient 
	protected Long nanoStart;
	protected Long queuedDurationNano;
	protected Long runningDurationNano;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_traceId = "traceId",
		COL_id = "id",
		COL_parentId = "parentId",
		COL_name = "name",
		COL_info = "info",
		COL_serverId = "serverId",
		COL_created = "created",
		COL_queuedDuration = "queuedDuration",
		COL_runningDuration = "runningDuration",
		COL_queuedDurationNano = "queuedDurationNano",
		COL_runningDurationNano = "runningDurationNano";

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new UInt63Field(COL_parentId, parentId),
				new StringField(COL_name, name),
				new StringField(COL_info, info),
				new StringField(COL_serverId, serverId),
				new UInt63Field(COL_created, created),
				new UInt63Field(COL_queuedDuration, queuedDuration),
				new UInt63Field(COL_runningDuration, runningDuration),
				new UInt63Field(COL_queuedDurationNano, queuedDurationNano),
				new UInt63Field(COL_runningDurationNano, runningDurationNano));
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	
	/*********************** constructor **********************************/
	
	TraceThread(){
		this.key = new TraceThreadKey(null, null);
	}
	
	public TraceThread(Long traceId, boolean hasParent){
		this.key = new TraceThreadKey(traceId, hasParent);
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public Class<TraceThreadKey> getKeyClass() {
		return TraceThreadKey.class;
	}
	
	@Override
	public TraceThreadKey getKey() {
		return key;
	}
	
	
	/***************************** compare ************************************/
	
	//not sure this is useful, but TraceThreadGroupComparator extends it
	public static class TraceThreadComparator implements Comparator<TraceThread>{
		@Override
		public int compare(TraceThread a, TraceThread b){
			int d0 = ComparableTool.nullFirstCompareTo(a.getParentId(), b.getParentId());
			if(d0 != 0){ return d0; }
			int d1 = ComparableTool.nullFirstCompareTo(a.getCreated(), b.getCreated());
			if(d1 != 0){ return d1; }
			return ComparableTool.nullFirstCompareTo(a.getName(), b.getName());
		}
	}
	
	
	/**************************** standard *****************************************/
	
	@Override
	public String toString(){
		return super.toString()+"["+name+"]";
	}
	

	/************************** methods ****************************************/
	
	public void markStart(){
		this.queuedDuration = System.currentTimeMillis() - this.created;
		this.queuedDurationNano = System.nanoTime() - this.nanoStart;
	}

	public void markFinish(){
		this.runningDuration = System.currentTimeMillis() - this.queuedDuration - this.created;
		this.runningDurationNano = System.nanoTime() - this.queuedDurationNano - this.nanoStart;
	}
	
	public Date getTime(){
		return new Date(this.created);
	}
	
	public Long getTotalDuration(){
		return getQueuedDuration() + getRunningDuration();
	}
	
	/********************************* get/set ****************************************/


	public void setKey(TraceThreadKey key) {
		this.key = key;
	}


	public String getServerId() {
		return serverId;
	}


	public void setServerId(String serverId) {
		this.serverId = serverId;
	}


	public Long getParentId() {
		return parentId;
	}


	public void setParentId(Long parentId) {
		this.parentId = parentId;
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



	public Long getId() {
		return key.getId();
	}


	public Long getTraceId() {
		return key.getTraceId();
	}


	public void setId(Long id) {
		key.setId(id);
	}


	public void setTraceId(Long traceId) {
		key.setTraceId(traceId);
	}


	public Long getQueuedDuration() {
		return queuedDuration;
	}


	public void setQueuedDuration(Long queuedDuration) {
		this.queuedDuration = queuedDuration;
	}


	public Long getRunningDuration() {
		return runningDuration;
	}


	public void setRunningDuration(Long runningDuration) {
		this.runningDuration = runningDuration;
	}


	public Long getQueuedDurationNano() {
		return queuedDurationNano;
	}


	public void setQueuedDurationNano(Long queuedDurationNano) {
		this.queuedDurationNano = queuedDurationNano;
	}


	public Long getRunningDurationNano() {
		return runningDurationNano;
	}


	public void setRunningDurationNano(Long runningDurationNano) {
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
