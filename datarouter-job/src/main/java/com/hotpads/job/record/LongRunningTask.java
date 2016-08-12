package com.hotpads.job.record;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;
import com.hotpads.datarouter.util.core.DrDateTool;

public class LongRunningTask extends BaseDatabean<LongRunningTaskKey,LongRunningTask>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	public static final long
			LAST_HEARTBEAT_WARNING_THRESHOLD = 2L * DrDateTool.MILLISECONDS_IN_SECOND,
			LAST_HEARTBEAT_STALLED_THRESHOLD = 10L * DrDateTool.MILLISECONDS_IN_SECOND;

	//used by longRunningTasks.jsp
	public static final int
			NULL = 3,
			OK = 0,
			WARNING = 1,
			STALLED = 2;

	private LongRunningTaskKey key;
	private LongRunningTaskType type;
	private Date startTime;
	private Boolean interrupt;
	private Date finishTime;
	private Date heartbeatTime;
	private JobExecutionStatus jobExecutionStatus;
	private String triggeredByUserEmail;
	private Long numItemsProcessed;

	/**************************** columns ****************************************/

	public static class FieldKeys{
		public static final StringEnumFieldKey<LongRunningTaskType> type = new StringEnumFieldKey<>("type",
				LongRunningTaskType.class);
		public static final DateFieldKey startTime = new DateFieldKey("startTime");
		public static final BooleanFieldKey interrupt = new BooleanFieldKey("interrupt");
		public static final DateFieldKey finishTime = new DateFieldKey("finishTime");
		public static final DateFieldKey heartbeatTime = new DateFieldKey("heartbeatTime");
		public static final StringEnumFieldKey<JobExecutionStatus> jobExecutionStatus = new StringEnumFieldKey<>(
				"jobExecutionStatus", JobExecutionStatus.class);
		public static final StringFieldKey triggeredByUserEmail = new StringFieldKey("triggeredByUserEmail");
		public static final LongFieldKey numItemsProcessed = new LongFieldKey("numItemsProcessed");
	}

	/********************** databean *****************************************/

	public static class LongRunningTaskFielder extends BaseDatabeanFielder<LongRunningTaskKey, LongRunningTask>{
		public LongRunningTaskFielder(){
			super(LongRunningTaskKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(LongRunningTask databean){
			return Arrays.asList(
					new StringEnumField<>(FieldKeys.type, databean.type),
					new DateField(FieldKeys.startTime, databean.startTime),
					new BooleanField(FieldKeys.interrupt, databean.interrupt),
					new DateField(FieldKeys.finishTime, databean.finishTime),
					new DateField(FieldKeys.heartbeatTime, databean.heartbeatTime),
					new StringEnumField<>(FieldKeys.jobExecutionStatus, databean.jobExecutionStatus),
					new StringField(FieldKeys.triggeredByUserEmail, databean.triggeredByUserEmail),
					new LongField(FieldKeys.numItemsProcessed, databean.numItemsProcessed));
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
		return DrDateTool.getAgoString(startTime);
	}

	public String getLastHeartbeatString(){
		if(heartbeatTime == null){
			return "";
		}
		return DrDateTool.getAgoString(heartbeatTime);
	}

	public String getFinishTimeString(){
		if(finishTime == null){
			return "";
		}
		return DrDateTool.getAgoString(finishTime);
	}

	public int getStatus(){
		if(heartbeatTime == null){
			return NULL;
		}
		long millisAgo = System.currentTimeMillis() - heartbeatTime.getTime();
		if(millisAgo > LAST_HEARTBEAT_STALLED_THRESHOLD){
			return STALLED;
		}else if(millisAgo > LAST_HEARTBEAT_WARNING_THRESHOLD){
			return WARNING;
		}else{
			return OK;
		}
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

	public LongRunningTaskType getType() {
		return type;
	}

	public Long getNumItemsProcessed() {
		return numItemsProcessed;
	}

	public void setNumItemsProcessed(Long numItemsProcessed) {
		this.numItemsProcessed = numItemsProcessed;
	}
}
