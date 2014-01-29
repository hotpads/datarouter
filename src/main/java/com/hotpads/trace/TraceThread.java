package com.hotpads.trace;

import java.util.Comparator;
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
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.util.core.ComparableTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class TraceThread extends BaseDatabean<TraceThreadKey,TraceThread>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	@Id
	protected TraceThreadKey key;

	protected Long parentId;
	@Column(length=DEFAULT_STRING_LENGTH)
	protected String name;
	protected String info;
	//should be serverName
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
	
	public static class F{
		public static final String
			KEY_NAME = "key",
			traceId = "traceId",
			id = "id",
			parentId = "parentId",
			name = "name",
			info = "info",
			serverId = "serverId",
			created = "created",
			queuedDuration = "queuedDuration",
			runningDuration = "runningDuration",
			queuedDurationNano = "queuedDurationNano",
			runningDurationNano = "runningDurationNano";
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new UInt63Field(F.parentId, parentId),
				new StringField(F.name, name, DEFAULT_STRING_LENGTH),
				new StringField(F.info, info, DEFAULT_STRING_LENGTH),
				new StringField(F.serverId, serverId,20),
				new UInt63Field(F.created, created),
				new UInt63Field(F.queuedDuration, queuedDuration),
				new UInt63Field(F.runningDuration, runningDuration),
				new UInt63Field(F.queuedDurationNano, queuedDurationNano),
				new UInt63Field(F.runningDurationNano, runningDurationNano));
	}
	
	public static class TraceThreadFielder extends BaseDatabeanFielder<TraceThreadKey,TraceThread>{
		public TraceThreadFielder(){}
		@Override
		public Class<TraceThreadKey> getKeyFielderClass(){
			return TraceThreadKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(TraceThread d){
			return d.getNonKeyFields();
		}
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

	//should be serverName
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
