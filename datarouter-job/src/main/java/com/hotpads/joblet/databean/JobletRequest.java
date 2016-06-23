package com.hotpads.joblet.databean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

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
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.exception.ExceptionRecordKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.datastructs.MutableBoolean;

@Entity
@Table(name="Joblet")//TODO change to JobletRequest
@AccessType("field")
@TypeDefs({
	@TypeDef(name="status", typeClass=com.hotpads.databean.GenericEnumUserType.class, parameters={
		@Parameter(name="enumClass", value="com.hotpads.joblet.enums.JobletStatus"),
		@Parameter(name="identifierMethod", value="getPersistentString"),
		@Parameter(name="valueOfMethod", value="fromPersistentStringStatic")}),
})
public class JobletRequest extends BaseDatabean<JobletRequestKey,JobletRequest>{

	@Id
	private JobletRequestKey key;
	private String queueId;
	@Type(type="status")
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
	@Transient
	private PhaseTimer timer = new PhaseTimer();
	@Transient
	private MutableBoolean interrupted;


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
		public static final StringFieldKey exceptionRecordId = new StringFieldKey("exceptionRecordId")
				.withSize(ExceptionRecordKey.LENGTH_id);
		public static final LongFieldKey jobletDataId = new LongFieldKey("jobletDataId");
		public static final IntegerFieldKey numItems = new IntegerFieldKey("numItems");
		public static final IntegerFieldKey numTasks = new IntegerFieldKey("numTasks");
		public static final StringFieldKey debug = new StringFieldKey("debug");
	}

	public static class JobletRequestFielder extends BaseDatabeanFielder<JobletRequestKey, JobletRequest> {
		public JobletRequestFielder() {
			super(JobletRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletRequest databean) {
			return Arrays.asList(
					new StringField(FieldKeys.queueId, databean.queueId),
					new StringEnumField<JobletStatus>(FieldKeys.status, databean.status),
					new IntegerField(FieldKeys.numFailures, databean.numFailures),
					new IntegerField(FieldKeys.numTimeouts, databean.numTimeouts),
					new StringField(FieldKeys.reservedBy, databean.reservedBy),
					new LongField(FieldKeys.reservedAt, databean.reservedAt),
					new BooleanField(FieldKeys.restartable, databean.restartable),
					new StringField(FieldKeys.exceptionRecordId, databean.exceptionRecordId),
					new LongField(FieldKeys.jobletDataId, databean.jobletDataId),
					new IntegerField(FieldKeys.numItems, databean.numItems),
					new IntegerField(FieldKeys.numTasks, databean.numTasks),
					new StringField(FieldKeys.debug, databean.debug));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}
	}


	/*-------------------- construct --------------------*/

	JobletRequest(){
		this.key = new JobletRequestKey(null, null, null);
	}

	public JobletRequest(JobletType<?> type, Integer executionOrder, Integer batchSequence, boolean restartable){
		this.key = new JobletRequestKey(type, executionOrder, batchSequence);
		this.restartable = restartable;
	}

	/*-------------------- databean --------------------*/

	@Override
	public Class<JobletRequestKey> getKeyClass(){
		return JobletRequestKey.class;
	}

	@Override
	public JobletRequestKey getKey() {
		return key;
	}

	/*----------------------------- static ----------------------------*/

	public static List<JobletSummary> getJobletCountsCreatedByType(JobletTypeFactory jobletTypeFactory,
			Iterable<JobletRequest> scanner){
		List<JobletSummary> summaries = new ArrayList<>();
		JobletType<?> currentType = null;
		Long oldestCreatedDate = null;
		Integer sumItems = 0;
		boolean atLeastOnecreatedJoblet = false;
		for(JobletRequest jobletRequest : scanner){
			JobletType<?> type = jobletTypeFactory.fromJobletRequest(jobletRequest);
			if(jobletRequest.getStatus() == JobletStatus.created){
				atLeastOnecreatedJoblet = true;
				if(currentType != null && type != currentType){
					summaries.add(new JobletSummary(currentType.getPersistentString(), sumItems, oldestCreatedDate));
					oldestCreatedDate = null;
					sumItems = 0;
				}
				currentType = jobletTypeFactory.fromJobletRequest(jobletRequest);
				sumItems = sumItems + jobletRequest.getNumItems();
				if(oldestCreatedDate == null || jobletRequest.getKey().getCreated() < oldestCreatedDate){
					oldestCreatedDate = jobletRequest.getKey().getCreated();
				}
			}
		}
        if(atLeastOnecreatedJoblet){
            summaries.add(new JobletSummary(currentType.getPersistentString(), sumItems, oldestCreatedDate));
        }
		return summaries;
	}

	public static ArrayList<JobletRequest> filterByTypeStatusReservedByPrefix(Iterable<JobletRequest> ins,
			JobletType<?> type, JobletStatus status, String reservedByPrefix){
		ArrayList<JobletRequest> outs = new ArrayList<>();
		for(JobletRequest in : DrIterableTool.nullSafe(ins)){
			if(type.getPersistentString() != in.getTypeString()) {
				continue;
			}
			if(status != in.getStatus()) {
				continue;
			}
			String reservedBy = DrStringTool.nullSafe(in.getReservedBy());
			if(!reservedBy.startsWith(reservedByPrefix)) {
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

    public int getMaxFailures(){
    	return 2;
    }

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

    /*-------------------- get/set --------------------*/

	public String getReservedBy() {
		return reservedBy;
	}

	public void setReservedBy(String reservedBy) {
		this.reservedBy = reservedBy;
	}

	public Long getReservedAt() {
		return reservedAt;
	}

	public void setReservedAt(Long reservedAt) {
		this.reservedAt = reservedAt;
	}

	public JobletStatus getStatus() {
		return status;
	}

	public void setStatus(JobletStatus status) {
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

	public Integer getNumTasks() {
		return numTasks;
	}

	public void setNumTasks(Integer numTasks) {
		this.numTasks = numTasks;
	}

	public Integer getNumFailures() {
		return numFailures;
	}

	public void setNumFailures(Integer numFailures) {
		this.numFailures = numFailures;
	}

	public Integer getNumTimeouts(){
		return numTimeouts;
	}

	public void setNumTimeouts(Integer numTimeouts){
		this.numTimeouts = numTimeouts;
	}

	public String getTypeString(){
		return key.getType();
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

	public PhaseTimer getTimer(){
		return timer;
	}

	public void setInterrupted(MutableBoolean interrupted){
		this.interrupted = interrupted;
	}

	public MutableBoolean getInterrupted(){
		return interrupted;
	}

	public void setTimer(PhaseTimer timer){
		this.timer = timer;
	}

}
