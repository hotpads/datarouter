package com.hotpads.joblet.databean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.joblet.type.JobletTypeFactory;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletRequest extends BaseDatabean<JobletRequestKey,JobletRequest>{

	public static final int MAX_FAILURES = 2;//TODO make this a custom field

	private JobletRequestKey key;
	private String queueId;
	private JobletStatus status = JobletStatus.created;
	private Integer numFailures = 0;
	private Integer numTimeouts = 0;
	private String reservedBy;
	private Long reservedAt;
	private Boolean restartable = false;
	private Long jobletDataId;
	private String exceptionRecordId;
	private Integer numItems = 0;
	private Integer numTasks = 0;
	private String debug;
	private String type;

	//TODO remove from the databean
	private QueueMessageKey queueMessageKey;//transient
	private MutableBoolean shutdownRequested;//a shared flag passed in from the executor

	public static final String KEY_NAME = "key";

	public static class FieldKeys{
		public static final StringFieldKey queueId = new StringFieldKey("queueId");
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
		public static final IntegerFieldKey numTasks = new IntegerFieldKey("numTasks");
		public static final StringFieldKey debug = new StringFieldKey("debug");
		public static final StringFieldKey type = new StringFieldKey("type");
	}

	public static class JobletRequestFielder extends BaseDatabeanFielder<JobletRequestKey,JobletRequest>{
		public JobletRequestFielder(){
			super(JobletRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletRequest databean){
			return Arrays.asList(
					new StringField(FieldKeys.queueId, databean.queueId),
					new StringEnumField<>(FieldKeys.status, databean.status),
					new IntegerField(FieldKeys.numFailures, databean.numFailures),
					new IntegerField(FieldKeys.numTimeouts, databean.numTimeouts),
					new StringField(FieldKeys.reservedBy, databean.reservedBy),
					new LongField(FieldKeys.reservedAt, databean.reservedAt),
					new BooleanField(FieldKeys.restartable, databean.restartable),
					new StringField(FieldKeys.exceptionRecordId, databean.exceptionRecordId),
					new LongField(FieldKeys.jobletDataId, databean.jobletDataId),
					new IntegerField(FieldKeys.numItems, databean.numItems),
					new IntegerField(FieldKeys.numTasks, databean.numTasks),
					new StringField(FieldKeys.debug, databean.debug),
					new StringField(FieldKeys.type, databean.type));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}
	}


	/*-------------------- construct --------------------*/

	public JobletRequest(){
		this.key = new JobletRequestKey(null, null, null, null);
	}

	public JobletRequest(JobletType<?> type, JobletPriority priority, Date createdDate, Integer batchSequence,
			boolean restartable){
		this.key = JobletRequestKey.create(type, priority.getExecutionOrder(), createdDate, batchSequence);
		this.type = type.getPersistentString();
		this.restartable = restartable;
	}

	/*-------------------- databean --------------------*/

	@Override
	public Class<JobletRequestKey> getKeyClass(){
		return JobletRequestKey.class;
	}

	@Override
	public JobletRequestKey getKey(){
		return key;
	}

	/*----------------------------- static ----------------------------*/

	public static ArrayList<JobletRequest> filterByTypeStatusReservedByPrefix(Iterable<JobletRequest> ins,
			JobletType<?> type, JobletStatus status, String reservedByPrefix){
		ArrayList<JobletRequest> outs = new ArrayList<>();
		for(JobletRequest in : DrIterableTool.nullSafe(ins)){
			if(type.getPersistentInt() != in.getKey().getTypeCode().intValue()){
				continue;
			}
			if(status != in.getStatus()){
				continue;
			}
			String reservedBy = DrStringTool.nullSafe(in.getReservedBy());
			if(!reservedBy.startsWith(reservedByPrefix)){
				continue;
			}
			outs.add(in);
		}
		return outs;
	}

	public static JobletRequest getOldestForTypesAndStatuses(JobletTypeFactory jobletTypeFactory,
			Iterable<JobletRequest> jobletRequests, Collection<JobletType<?>> types, Collection<JobletStatus> statuses){
		JobletRequest oldest = null;
		long now = System.currentTimeMillis();
		for(JobletRequest jobletRequest : DrIterableTool.nullSafe(jobletRequests)){
			JobletType<?> jobletType = jobletTypeFactory.fromJobletRequest(jobletRequest);
			if(types.contains(jobletType) && statuses.contains(jobletRequest.getStatus())){
				if(oldest == null){
					oldest = jobletRequest;
				}
				long ageMs = now - jobletRequest.getKey().getCreated();
				long oldestAgeMs = now - oldest.getKey().getCreated();
				if(ageMs > oldestAgeMs){
					oldest = jobletRequest;
				}
			}
		}
		return oldest;
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
		return DrDateTool.getAgoString(this.getKey().getCreated());
	}

	public Date getReservedAtDate(){
		return reservedAt == null ? null : new Date(reservedAt);
	}

	public Optional<Long> getReservedAgoMs(){
		return reservedAt == null ? Optional.empty() : Optional.of(System.currentTimeMillis() - reservedAt);
	}

	public int incrementNumFailures(){
		numFailures = DrNumberTool.nullSafe(numFailures) + 1;
		return numFailures;
	}

	public boolean hasReachedMaxFailures(){
		return numFailures >= MAX_FAILURES;
	}

	public int incrementNumTimeouts(){
		numTimeouts = DrNumberTool.nullSafe(numTimeouts) + 1;
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

	public Integer getNumTasks(){
		return numTasks;
	}

	public void setNumTasks(Integer numTasks){
		this.numTasks = numTasks;
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

	public String getTypeString(){
		return type;
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

}
