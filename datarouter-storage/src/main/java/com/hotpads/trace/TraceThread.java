package com.hotpads.trace;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.trace.key.TraceThreadKey;

public class TraceThread extends BaseDatabean<TraceThreadKey,TraceThread>{

	private TraceThreadKey key;

	private Long parentId;
	private String name;
	private String info;
	private String serverId;//should be serverName
	private Long created;
	private Long queuedDuration;
	private Long runningDuration;
	private Long nanoStart;
	private Long queuedDurationNano;
	private Long runningDurationNano;


	/**************************** columns *******************************/

	public static class FieldKeys{
		public static final UInt63FieldKey parentId = new UInt63FieldKey("parentId");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey info = new StringFieldKey("info");
		public static final StringFieldKey serverId = new StringFieldKey("serverId").withSize(20);
		public static final UInt63FieldKey created = new UInt63FieldKey("created");
		public static final UInt63FieldKey queuedDuration = new UInt63FieldKey("queuedDuration");
		public static final UInt63FieldKey runningDuration = new UInt63FieldKey("runningDuration");
		public static final UInt63FieldKey queuedDurationNano = new UInt63FieldKey("queuedDurationNano");
		public static final UInt63FieldKey runningDurationNano = new UInt63FieldKey("runningDurationNano");
	}


	public static class TraceThreadFielder extends BaseDatabeanFielder<TraceThreadKey,TraceThread>{
		public TraceThreadFielder(){
			super(TraceThreadKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(TraceThread traceThread){
			return Arrays.asList(
					new UInt63Field(FieldKeys.parentId, traceThread.parentId),
					new StringField(FieldKeys.name, traceThread.name),
					new StringField(FieldKeys.info, traceThread.info),
					new StringField(FieldKeys.serverId, traceThread.serverId),
					new UInt63Field(FieldKeys.created, traceThread.created),
					new UInt63Field(FieldKeys.queuedDuration, traceThread.queuedDuration),
					new UInt63Field(FieldKeys.runningDuration, traceThread.runningDuration),
					new UInt63Field(FieldKeys.queuedDurationNano, traceThread.queuedDurationNano),
					new UInt63Field(FieldKeys.runningDurationNano, traceThread.runningDurationNano));
		}
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
		public int compare(TraceThread threadA, TraceThread threadB){
			int d0 = DrComparableTool.nullFirstCompareTo(threadA.getParentId(), threadB.getParentId());
			if(d0 != 0){
				return d0;
			}
			int d1 = DrComparableTool.nullFirstCompareTo(threadA.getCreated(), threadB.getCreated());
			if(d1 != 0){
				return d1;
			}
			return DrComparableTool.nullFirstCompareTo(threadA.getName(), threadB.getName());
		}
	}


	/**************************** standard *****************************************/

	@Override
	public String toString(){
		return super.toString() + "[" + name + "]";
	}


	/************************** methods ****************************************/

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


	/********************************* get/set ****************************************/

	public void setKey(TraceThreadKey key){
		this.key = key;
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

	public Long getId(){
		return key.getId();
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
