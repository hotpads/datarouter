package com.hotpads.job.record;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.util.core.DateTool;

public class LongRunningTask extends BaseDatabean<LongRunningTaskKey,LongRunningTask>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final long LAST_HEARTBEAT_WARNING_THRESHOLD = 2l * DateTool.MILLISECONDS_IN_SECOND,
							LAST_HEARTBEAT_STALLED_THRESHOLD = 10l * DateTool.MILLISECONDS_IN_SECOND;
	
	private LongRunningTaskKey key;
	private LongRunningTaskType type;
	private Date startTime;
	private Boolean interrupt;
	private Date finishTime;
	private Date heartbeatTime;
	private JobExecutionStatus jobExecutionStatus;
	private String triggeredByUserEmail;
	private Integer numItemsProcessed;
	
	/**************************** columns ****************************************/
	
	public static class F{
		public static final String
			startTime = "startTime",
			interrupt = "interrupt",
			finishTime = "finishTime",
			heartbeatTime = "heartbeatTime",
			jobExecutionStatus = "jobExecutionStatus",
			triggeredByUserEmail = "triggeredByUserEmail",
			type = "type",
			numItemsProcessed = "numItemsProcessed";
	}
	
	/********************** databean *****************************************/
	
	public static class LongRunningTaskFielder extends BaseDatabeanFielder<LongRunningTaskKey, LongRunningTask>{
		public LongRunningTaskFielder(){}
		@Override
		public Class<LongRunningTaskKey> getKeyFielderClass(){
			return LongRunningTaskKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(LongRunningTask d){
			return FieldTool.createList(
					new StringEnumField<LongRunningTaskType>(LongRunningTaskType.class, F.type, d.type, DEFAULT_STRING_LENGTH),
					new DateField(F.startTime, d.startTime),
					new BooleanField(F.interrupt, d.interrupt),
					new DateField(F.finishTime, d.finishTime),
					new DateField(F.heartbeatTime, d.heartbeatTime),
					new StringEnumField<JobExecutionStatus>(JobExecutionStatus.class, F.jobExecutionStatus, d.jobExecutionStatus, DEFAULT_STRING_LENGTH),
					new StringField(F.triggeredByUserEmail, d.triggeredByUserEmail, DEFAULT_STRING_LENGTH),
					new IntegerField(F.numItemsProcessed, d.numItemsProcessed));
		}
	}
	
	@Override
	public Class<LongRunningTaskKey> getKeyClass() {
		return LongRunningTaskKey.class;
	}

	@Override
	public LongRunningTaskKey getKey(){
		return key;
	}
	
	/****************** construct ************************/
	
	public LongRunningTask(){
		this.key = new LongRunningTaskKey();
	}
	
	public LongRunningTask(String jobClass, String serverName, LongRunningTaskType type){
		this.key = new LongRunningTaskKey(jobClass, serverName);
		this.type = type;
	}
	
	/****************** helper methods ************************/
	
	public String getDurationString(){
		return DateTool.getAgoString(startTime);
	}
	
	public String getLastHeartbeatString(){
		if(heartbeatTime == null){
			return "";
		}
		return DateTool.getAgoString(heartbeatTime);
	}
	
	public int getStatus(){
		if(heartbeatTime == null){
			return 0;
		}
		long millisAgo = System.currentTimeMillis() - heartbeatTime.getTime();
		if(millisAgo > LAST_HEARTBEAT_STALLED_THRESHOLD){
			return 2;
		}
		else if(millisAgo > LAST_HEARTBEAT_WARNING_THRESHOLD){
			return 1;
		}
		return 0;
	}
	
	/****************** get/set ************************/
	
	public void setTriggerTime(Date date){
		key.setTriggerTime(date);
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Boolean getInterrupt() {
		return interrupt;
	}

	public void setInterrupt(Boolean interrupt) {
		this.interrupt = interrupt;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Date getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(Date heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public JobExecutionStatus getJobExecutionStatus() {
		return jobExecutionStatus;
	}

	public void setJobExecutionStatus(JobExecutionStatus jobExecutionStatus) {
		this.jobExecutionStatus = jobExecutionStatus;
	}

	public String getTriggeredByUserEmail() {
		return triggeredByUserEmail;
	}

	public void setTriggeredByUserEmail(String triggeredByUserEmail) {
		this.triggeredByUserEmail = triggeredByUserEmail;
	}

	public void setKey(LongRunningTaskKey key) {
		this.key = key;
	}

	public LongRunningTaskType getType() {
		return type;
	}

	public void setType(LongRunningTaskType type) {
		this.type = type;
	}

	public Integer getNumItemsProcessed() {
		return numItemsProcessed;
	}

	public void setNumItemsProcessed(Integer numItemsProcessed) {
		this.numItemsProcessed = numItemsProcessed;
	}
}
