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
package io.datarouter.tasktracker.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskInfo;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.DateTool;
import io.datarouter.util.time.DurationTool;

public class LongRunningTask extends BaseDatabean<LongRunningTaskKey,LongRunningTask>{

	public static final Duration HEARTBEAT_STATUS_WARNING = Duration.ofSeconds(2);
	public static final Duration HEARTBEAT_STATUS_STALLED = Duration.ofSeconds(10);

	private LongRunningTaskType type;
	private Date startTime;
	private Date finishTime;
	private Date heartbeatTime;
	private LongRunningTaskStatus jobExecutionStatus;
	private String triggeredBy;
	private Long numItemsProcessed;
	private String lastItemProcessed;

	public static class FieldKeys{
		public static final StringEnumFieldKey<LongRunningTaskType> type = new StringEnumFieldKey<>("type",
				LongRunningTaskType.class);
		public static final DateFieldKey startTime = new DateFieldKey("startTime");
		public static final BooleanFieldKey interrupt = new BooleanFieldKey("interrupt");
		public static final DateFieldKey finishTime = new DateFieldKey("finishTime");
		public static final DateFieldKey heartbeatTime = new DateFieldKey("heartbeatTime");
		public static final StringEnumFieldKey<LongRunningTaskStatus> longRunningTaskStatus = new StringEnumFieldKey<>(
				"jobExecutionStatus", LongRunningTaskStatus.class);
		public static final StringFieldKey triggeredBy = new StringFieldKey("triggeredBy");
		public static final LongFieldKey numItemsProcessed = new LongFieldKey("numItemsProcessed");
		private static final StringFieldKey lastItemProcessed = new StringFieldKey("lastItemProcessed")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
	}

	public static class LongRunningTaskFielder extends BaseDatabeanFielder<LongRunningTaskKey,LongRunningTask>{
		public LongRunningTaskFielder(){
			super(LongRunningTaskKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(LongRunningTask databean){
			return Arrays.asList(
					new StringEnumField<>(FieldKeys.type, databean.type),
					new DateField(FieldKeys.startTime, databean.startTime),
					new DateField(FieldKeys.finishTime, databean.finishTime),
					new DateField(FieldKeys.heartbeatTime, databean.heartbeatTime),
					new StringEnumField<>(FieldKeys.longRunningTaskStatus, databean.jobExecutionStatus),
					new StringField(FieldKeys.triggeredBy, databean.triggeredBy),
					new LongField(FieldKeys.numItemsProcessed, databean.numItemsProcessed),
					new StringField(FieldKeys.lastItemProcessed, databean.lastItemProcessed));
		}
	}

	public LongRunningTask(){
		super(new LongRunningTaskKey());
	}

	public LongRunningTask(LongRunningTaskInfo task){
		super(new LongRunningTaskKey(task.name, task.triggerTime, task.serverName));
		this.type = task.type;
		this.startTime = task.startTime;
		this.finishTime = task.finishTime;
		this.heartbeatTime = task.heartbeatTime;
		this.jobExecutionStatus = task.longRunningTaskStatus;
		this.triggeredBy = task.triggeredBy;
		this.numItemsProcessed = task.numItemsProcessed;
		this.lastItemProcessed = task.lastItemProcessed;
	}

	@Override
	public Class<LongRunningTaskKey> getKeyClass(){
		return LongRunningTaskKey.class;
	}

	public Duration getDuration(){
		if(startTime == null){
			return null;
		}
		Instant from = startTime.toInstant();
		Instant to;
		if(finishTime != null){
			to = finishTime.toInstant();
		}else if(jobExecutionStatus == LongRunningTaskStatus.RUNNING){
			to = Instant.now();
		}else if(heartbeatTime != null){
			to = heartbeatTime.toInstant();
		}else{
			return null;
		}
		return Duration.between(from, to);
	}

	public String getDurationString(){
		if(startTime == null){
			return "";
		}
		Duration duration = getDuration();
		if(duration == null){
			return "Unknown";
		}
		return DurationTool.toString(duration);
	}

	public String getLastHeartbeatString(){
		if(heartbeatTime == null){
			return "";
		}
		return DateTool.getAgoString(heartbeatTime.toInstant());
	}

	public String getFinishTimeString(){
		if(finishTime == null){
			return "";
		}
		return DateTool.getAgoString(finishTime.toInstant());
	}

	public boolean isRunning(){
		return jobExecutionStatus == LongRunningTaskStatus.RUNNING;
	}

	public boolean isSuccess(){
		return jobExecutionStatus == LongRunningTaskStatus.SUCCESS;
	}

	public String getHeartbeatStatus(){
		if(heartbeatTime == null || !isRunning()){
			return null;
		}
		Duration elapsed = DurationTool.sinceDate(heartbeatTime);
		if(ComparableTool.gt(elapsed, HEARTBEAT_STATUS_STALLED)){
			return "stalled";
		}else if(ComparableTool.gt(elapsed, HEARTBEAT_STATUS_WARNING)){
			return "warning";
		}else{
			return "ok";
		}
	}

	public Date getStartTime(){
		return startTime;
	}

	public void setStartTime(Date startTime){
		this.startTime = startTime;
	}

	public Date getFinishTime(){
		return finishTime;
	}

	public void setFinishTime(Date finishTime){
		this.finishTime = finishTime;
	}

	public Date getHeartbeatTime(){
		return heartbeatTime;
	}

	public void setHeartbeatTime(Date heartbeatTime){
		this.heartbeatTime = heartbeatTime;
	}

	public LongRunningTaskStatus getJobExecutionStatus(){
		return jobExecutionStatus;
	}

	public void setJobExecutionStatus(LongRunningTaskStatus longRunningTaskStatus){
		this.jobExecutionStatus = longRunningTaskStatus;
	}

	public String getTriggeredBy(){
		return triggeredBy;
	}

	public LongRunningTaskType getType(){
		return type;
	}

	public Long getNumItemsProcessed(){
		return numItemsProcessed;
	}

	public void setNumItemsProcessed(Long numItemsProcessed){
		this.numItemsProcessed = numItemsProcessed;
	}

	public String getLastItemProcessed(){
		return lastItemProcessed;
	}

	public void setLastItemProcessed(String lastItemProcessed){
		this.lastItemProcessed = lastItemProcessed;
	}

}
