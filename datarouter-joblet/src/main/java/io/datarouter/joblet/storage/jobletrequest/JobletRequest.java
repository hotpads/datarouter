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
package io.datarouter.joblet.storage.jobletrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletdata.JobletDataKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.DateTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.mutable.MutableBoolean;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;

public class JobletRequest extends BaseDatabean<JobletRequestKey,JobletRequest>{

	public static final int MAX_FAILURES = 2;//TODO make this a custom field

	/**
	 * an arbitrary tag that be used to group joblets for monitoring
	 * TODO rename to category or something similar
	 */
	private String queueId;
	private String groupId;
	private JobletStatus status = JobletStatus.CREATED;
	private Integer numFailures = 0;
	private Integer numTimeouts = 0;
	private String reservedBy;
	private Long reservedAt;
	private Boolean restartable = false;
	private Long jobletDataId;
	private String exceptionRecordId;
	private Integer numItems = 0;
	private String debug;
	private Long dataSignature;

	//TODO remove from the databean
	private QueueMessageKey queueMessageKey;//transient
	private MutableBoolean shutdownRequested;//a shared flag passed in from the executor

	public static final String KEY_NAME = "key";

	public static class FieldKeys{
		public static final StringFieldKey queueId = new StringFieldKey("queueId");
		public static final StringFieldKey groupId = new StringFieldKey("groupId");
		public static final StringEnumFieldKey<JobletStatus> status = new StringEnumFieldKey<>("status",
				JobletStatus.class);
		public static final IntegerFieldKey numFailures = new IntegerFieldKey("numFailures");
		public static final IntegerFieldKey numTimeouts = new IntegerFieldKey("numTimeouts");
		public static final StringFieldKey reservedBy = new StringFieldKey("reservedBy");
		public static final LongFieldKey reservedAt = new LongFieldKey("reservedAt");
		public static final BooleanFieldKey restartable = new BooleanFieldKey("restartable");
		public static final StringFieldKey exceptionRecordId = new StringFieldKey("exceptionRecordId");
		public static final LongFieldKey jobletDataId = new LongFieldKey("jobletDataId");
		public static final IntegerFieldKey numItems = new IntegerFieldKey("numItems");
		public static final StringFieldKey debug = new StringFieldKey("debug");
		public static final LongFieldKey dataSignature = new LongFieldKey("dataSignature");
	}

	public static class JobletRequestFielder extends BaseDatabeanFielder<JobletRequestKey,JobletRequest>{
		public JobletRequestFielder(){
			super(JobletRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletRequest databean){
			return Arrays.asList(
					new StringField(FieldKeys.queueId, databean.queueId),
					new StringField(FieldKeys.groupId, databean.groupId),
					new StringEnumField<>(FieldKeys.status, databean.status),
					new IntegerField(FieldKeys.numFailures, databean.numFailures),
					new IntegerField(FieldKeys.numTimeouts, databean.numTimeouts),
					new StringField(FieldKeys.reservedBy, databean.reservedBy),
					new LongField(FieldKeys.reservedAt, databean.reservedAt),
					new BooleanField(FieldKeys.restartable, databean.restartable),
					new StringField(FieldKeys.exceptionRecordId, databean.exceptionRecordId),
					new LongField(FieldKeys.jobletDataId, databean.jobletDataId),
					new IntegerField(FieldKeys.numItems, databean.numItems),
					new StringField(FieldKeys.debug, databean.debug),
					new LongField(FieldKeys.dataSignature, databean.dataSignature));
		}

	}

	/*-------------------- construct --------------------*/

	public JobletRequest(){
		super(new JobletRequestKey((String) null, null, null, null));
	}

	public JobletRequest(JobletType<?> type, JobletPriority priority, Date createdDate, Integer batchSequence,
			boolean restartable, Long dataSignature){
		super(JobletRequestKey.create(type, priority.getExecutionOrder(), createdDate, batchSequence));
		this.restartable = restartable;
		this.dataSignature = dataSignature;
	}

	/*-------------------- databean --------------------*/

	@Override
	public Class<JobletRequestKey> getKeyClass(){
		return JobletRequestKey.class;
	}

	/*----------------------------- static ----------------------------*/

	public static ArrayList<JobletRequest> filterByTypeStatusReservedByPrefix(Iterable<JobletRequest> ins,
			JobletType<?> type, JobletStatus status, String reservedByPrefix){
		ArrayList<JobletRequest> outs = new ArrayList<>();
		for(JobletRequest in : IterableTool.nullSafe(ins)){
			if(ObjectTool.notEquals(type.getPersistentString(), in.getKey().getType())){
				continue;
			}
			if(status != in.getStatus()){
				continue;
			}
			String reservedBy = StringTool.nullSafe(in.getReservedBy());
			if(!reservedBy.startsWith(reservedByPrefix)){
				continue;
			}
			outs.add(in);
		}
		return outs;
	}

	public static List<JobletDataKey> getJobletDataKeys(List<JobletRequest> jobletRequests){
		return jobletRequests.stream()
				.map(JobletRequest::getJobletDataKey)
				.collect(Collectors.toList());
	}

    /*-------------------- methods --------------------*/

	public JobletDataKey getJobletDataKey(){
		return new JobletDataKey(jobletDataId);
	}

	public String getCreatedAgo(){
		if(this.getKey().getCreated() == null){
			return "";
		}
		return DateTool.getAgoString(this.getKey().getCreated());
	}

	public Date getReservedAtDate(){
		return reservedAt == null ? null : new Date(reservedAt);
	}

	public Optional<Long> getReservedAgoMs(){
		return reservedAt == null ? Optional.empty() : Optional.of(System.currentTimeMillis() - reservedAt);
	}

	public int incrementNumFailures(){
		numFailures = NumberTool.nullSafe(numFailures) + 1;
		return numFailures;
	}

	public boolean hasReachedMaxFailures(){
		return numFailures >= MAX_FAILURES;
	}

	public int incrementNumTimeouts(){
		numTimeouts = NumberTool.nullSafe(numTimeouts) + 1;
		return numTimeouts;
	}

    /*-------------------- get/set --------------------*/

	public String getReservedBy(){
		return reservedBy;
	}

	public void setReservedBy(String reservedBy){
		this.reservedBy = reservedBy;
	}

	public Long getReservedAt(){
		return reservedAt;
	}

	public void setReservedAt(Long reservedAt){
		this.reservedAt = reservedAt;
	}

	public JobletStatus getStatus(){
		return status;
	}

	public void setStatus(JobletStatus status){
		this.status = status;
	}

	public Integer getNumItems(){
		return numItems;
	}

	public void setNumItems(Integer numItems){
		this.numItems = numItems;
	}

	public Boolean getRestartable(){
		return restartable;
	}

	public Integer getNumFailures(){
		return numFailures;
	}

	public void setNumFailures(Integer numFailures){
		this.numFailures = numFailures;
	}

	public Integer getNumTimeouts(){
		return numTimeouts;
	}

	public void setNumTimeouts(Integer numTimeouts){
		this.numTimeouts = numTimeouts;
	}

	public Long getJobletDataId(){
		return jobletDataId;
	}

	public void setJobletDataId(Long jobletDataId){
		this.jobletDataId = jobletDataId;
	}

	public String getExceptionRecordId(){
		return exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId){
		this.exceptionRecordId = exceptionRecordId;
	}

	public String getDebug(){
		return debug;
	}

	public String getQueueId(){
		return queueId;
	}

	public void setQueueId(String queueId){
		this.queueId = queueId;
	}

	public void setShutdownRequested(MutableBoolean shutdownRequested){
		this.shutdownRequested = shutdownRequested;
	}

	public MutableBoolean getShutdownRequested(){
		return shutdownRequested;
	}

	public QueueMessageKey getQueueMessageKey(){
		return queueMessageKey;
	}

	public void setQueueMessageKey(QueueMessageKey queueMessageKey){
		this.queueMessageKey = queueMessageKey;
	}

	public Long getDataSignature(){
		return dataSignature;
	}

	public void setDataSignature(Long dataSignature){
		this.dataSignature = dataSignature;
	}

	public String getGroupId(){
		return groupId;
	}

	public void setGroupId(String groupId){
		this.groupId = groupId;
	}

}
