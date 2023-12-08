/*
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
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.instrumentation.task.TaskTrackerDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskInfo;
import io.datarouter.types.MilliTime;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.time.DurationTool;

public class LongRunningTask extends BaseDatabean<LongRunningTaskKey,LongRunningTask>{

	public static final Duration HEARTBEAT_STATUS_WARNING = Duration.ofSeconds(2);
	public static final Duration HEARTBEAT_STATUS_STALLED = Duration.ofSeconds(10);

	private LongRunningTaskType type;
	private MilliTime start;
	private MilliTime finish;
	private MilliTime heartbeat;
	private LongRunningTaskStatus jobExecutionStatus;
	private String triggeredBy;
	private Long numItemsProcessed;
	private String lastItemProcessed;
	private String exceptionRecordId;

	public static class FieldKeys{
		public static final StringEncodedFieldKey<LongRunningTaskType> type = new StringEncodedFieldKey<>(
				"type",
				new StringMappedEnumFieldCodec<>(LongRunningTaskType.BY_PERSISTENT_STRING));
		public static final LongEncodedFieldKey<MilliTime> start = new LongEncodedFieldKey<>(
				"start",
				new MilliTimeFieldCodec());
		public static final BooleanFieldKey interrupt = new BooleanFieldKey("interrupt");
		public static final LongEncodedFieldKey<MilliTime> finish = new LongEncodedFieldKey<>(
				"finish",
				new MilliTimeFieldCodec());
		public static final LongEncodedFieldKey<MilliTime> heartbeat = new LongEncodedFieldKey<>(
				"heartbeat",
				new MilliTimeFieldCodec());
		public static final StringEncodedFieldKey<LongRunningTaskStatus> longRunningTaskStatus
				= new StringEncodedFieldKey<>(
				"jobExecutionStatus",
				new StringMappedEnumFieldCodec<>(LongRunningTaskStatus.BY_PERSISTENT_STRING));
		public static final StringFieldKey triggeredBy = new StringFieldKey("triggeredBy");
		public static final LongFieldKey numItemsProcessed = new LongFieldKey("numItemsProcessed");
		public static final StringFieldKey lastItemProcessed = new StringFieldKey("lastItemProcessed")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey exceptionRecordId = new StringFieldKey("exceptionRecordId");
	}

	public static class LongRunningTaskFielder extends BaseDatabeanFielder<LongRunningTaskKey,LongRunningTask>{

		public LongRunningTaskFielder(){
			super(LongRunningTaskKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(LongRunningTask databean){
			return List.of(
					new StringEncodedField<>(FieldKeys.type, databean.type),
					new LongEncodedField<>(FieldKeys.start, databean.start),
					new LongEncodedField<>(FieldKeys.finish, databean.finish),
					new LongEncodedField<>(FieldKeys.heartbeat, databean.heartbeat),
					new StringEncodedField<>(FieldKeys.longRunningTaskStatus, databean.jobExecutionStatus),
					new StringField(FieldKeys.triggeredBy, databean.triggeredBy),
					new LongField(FieldKeys.numItemsProcessed, databean.numItemsProcessed),
					new StringField(FieldKeys.lastItemProcessed, databean.lastItemProcessed),
					new StringField(FieldKeys.exceptionRecordId, databean.exceptionRecordId));
		}
	}

	public LongRunningTask(){
		super(new LongRunningTaskKey());
	}

	public LongRunningTask(LongRunningTaskInfo task){
		super(new LongRunningTaskKey(task.name, MilliTime.ofEpochMilli(task.triggerTimeMs), task.serverName));
		this.type = task.type;
		this.start = Optional.ofNullable(task.startTimeMs)
				.map(MilliTime::ofEpochMilli)
				.orElse(null);
		this.finish = Optional.ofNullable(task.finishTimeMs)
				.map(MilliTime::ofEpochMilli)
				.orElse(null);
		this.heartbeat = Optional.ofNullable(task.heartbeatTimeMs)
				.map(MilliTime::ofEpochMilli)
				.orElse(null);
		this.jobExecutionStatus = task.longRunningTaskStatus;
		this.triggeredBy = task.triggeredBy;
		this.numItemsProcessed = task.numItemsProcessed.get();
		this.lastItemProcessed = task.lastItemProcessed;
		this.exceptionRecordId = task.exceptionRecordId;
	}

	@Override
	public Supplier<LongRunningTaskKey> getKeySupplier(){
		return LongRunningTaskKey::new;
	}

	public Duration getDuration(){
		MilliTime from = start != null
				? start
				: getKey().getTriggerTime();
		MilliTime to;
		if(finish != null){
			to = finish;
		}else if(jobExecutionStatus == LongRunningTaskStatus.RUNNING){
			to = MilliTime.now();
		}else if(heartbeat != null){
			to = heartbeat;
		}else{
			return null;
		}
		long differenceMs = to.minus(from).toEpochMilli();
		return Duration.ofMillis(differenceMs);
	}

	public String getDurationString(){
		Duration duration = getDuration();
		if(duration == null){
			return "Unknown";
		}
		return DurationTool.toString(duration);
	}

	public String getLastHeartbeatString(ZoneId zoneId){
		if(heartbeat == null){
			return "";
		}
		return heartbeat.format(zoneId);
	}

	public String getFinishTimeString(ZoneId zoneId){
		if(finish == null){
			return "";
		}
		return finish.format(zoneId);
	}

	public boolean isRunning(){
		return jobExecutionStatus == LongRunningTaskStatus.RUNNING;
	}

	public boolean isSuccess(){
		return jobExecutionStatus == LongRunningTaskStatus.SUCCESS;
	}

	public boolean isBadState(){
		return jobExecutionStatus.isBadState;
	}

	public LongRunningTaskHeartBeatStatus getHeartbeatStatus(){
		if(heartbeat == null || !isRunning()){
			return null;
		}
		Duration elapsed = DurationTool.sinceDate(heartbeat.toDate());
		if(ComparableTool.gt(elapsed, HEARTBEAT_STATUS_STALLED)){
			return LongRunningTaskHeartBeatStatus.STALLED;
		}else if(ComparableTool.gt(elapsed, HEARTBEAT_STATUS_WARNING)){
			return LongRunningTaskHeartBeatStatus.WARNING;
		}else{
			return LongRunningTaskHeartBeatStatus.OK;
		}
	}

	public MilliTime getStart(){
		return start;
	}

	public Optional<MilliTime> findStart(){
		return Optional.ofNullable(start);
	}

	public void setStartTime(MilliTime start){
		this.start = start;
	}

	public MilliTime getFinish(){
		return finish;
	}

	public void setFinishTime(MilliTime finish){
		this.finish = finish;
	}

	public MilliTime getHeartbeat(){
		return heartbeat;
	}

	public void setHeartbeatTime(MilliTime heartbeat){
		this.heartbeat = heartbeat;
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

	public String getExceptionRecordId(){
		return exceptionRecordId;
	}

	public TaskTrackerDto toDto(){
		return new TaskTrackerDto(
				getKey().toDto(),
				type.persistentString,
				Optional.ofNullable(start)
						.map(MilliTime::toInstant)
						.orElse(null),
				Optional.ofNullable(finish)
						.map(MilliTime::toInstant)
						.orElse(null),
				Optional.ofNullable(heartbeat)
						.map(MilliTime::toInstant)
						.orElse(null),
				jobExecutionStatus.persistentString,
				triggeredBy,
				numItemsProcessed,
				lastItemProcessed,
				exceptionRecordId);
	}

}
