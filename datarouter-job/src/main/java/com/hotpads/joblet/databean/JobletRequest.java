package com.hotpads.joblet.databean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.exception.ExceptionRecordKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.core.stream.StreamTool;
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

	/************************** enums **************************************/


	/*********************************** fields **************************/

	@Id
	protected JobletRequestKey key;
	protected String queueId;
	@Type(type="status")
	protected JobletStatus status = JobletStatus.created;
	protected Integer numFailures = 0;
	protected Integer numTimeouts = 0;
	protected String reservedBy;
	protected Long reservedAt;
	protected Boolean restartable = false;
	protected Long jobletDataId;
	private String exceptionRecordId;
	protected Integer numItems = 0;
	protected Integer numTasks = 0;
	protected String debug;
	@Transient
	protected Boolean unmarshalled = false;
	@Transient
	protected boolean deleted = false;
	@Transient
	protected PhaseTimer timer = new PhaseTimer();
	@Transient
	protected MutableBoolean interrupted;

    /************************** columns *******************************/

	public static final String KEY_NAME = "key";

	public class F{
		public static final String
			type = "type",
			executionOrder = "executionOrder",
	    	created = "created",
			batchSequence = "batchSequence",
	    	queueId = "queueId",
	    	status = "status",
	    	reservedBy = "reservedBy",
	    	reservedAt = "reservedAt",
	    	restartable = "restartable",
	    	jobletDataId = "jobletDataId",
	    	exceptionRecordId = "exceptionRecordId",
	    	numItems = "numItems",
	    	numTasks = "numTasks",
	    	numFailures = "numFailures",
	    	numTimeouts = "numTimeouts",
	    	createdBy = "createdBy",
	    	debug = "debug";
	}


	/************************* constructors ********************************/

	JobletRequest(){
		this.key = new JobletRequestKey(null, null, null);
	}

	public JobletRequest(JobletType<?> type, Integer executionOrder, Integer batchSequence, boolean restartable){
		this.key = new JobletRequestKey(type, executionOrder, batchSequence);
		this.restartable = restartable;
	}

	/******************** databean ************************************/

	public static class JobletFielder extends BaseDatabeanFielder<JobletRequestKey, JobletRequest> {
		public JobletFielder() {
			super(JobletRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobletRequest databean) {
			return Arrays.asList(
					new StringField(F.queueId, databean.queueId, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringEnumField<JobletStatus>(JobletStatus.class, F.status, databean.status,
							MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.numFailures, databean.numFailures),
					new IntegerField(F.numTimeouts, databean.numTimeouts),
					new StringField(F.reservedBy, databean.reservedBy, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new LongField(F.reservedAt, databean.reservedAt),
					new BooleanField(F.restartable, databean.restartable),
					new StringField(F.exceptionRecordId, databean.exceptionRecordId, ExceptionRecordKey.LENGTH_id),
					new LongField(F.jobletDataId, databean.jobletDataId),
					new IntegerField(F.numItems, databean.numItems),
					new IntegerField(F.numTasks, databean.numTasks),
					new StringField(F.debug, databean.debug, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	@Override
	public Class<JobletRequestKey> getKeyClass(){
		return JobletRequestKey.class;
	}

	@Override
	public JobletRequestKey getKey() {
		return key;
	}

    /*********************************** methods ***************************/

    public int getMaxFailures(){
    	return 2;
    }

	public JobletDataKey getJobletDataKey(){
		return new JobletDataKey(this.jobletDataId);
	}

	public static List<JobletSummary> getJobletCountsCreatedByType(JobletTypeFactory jobletTypeFactory,
			Iterable<JobletRequest> scanner){
		List<JobletSummary> summaries = new ArrayList<>();
		JobletType<?> currentType = null;
		Long oldestCreatedDate = null;
		Integer sumItems = 0;
		boolean atLeastOnecreatedJoblet = false;
		for(JobletRequest joblet : scanner){
			JobletType<?> type = jobletTypeFactory.fromJobletRequest(joblet);
			if(joblet.getStatus() == JobletStatus.created){
				atLeastOnecreatedJoblet = true;
				if(currentType != null && type != currentType){
					summaries.add(new JobletSummary(currentType.getPersistentString(), sumItems, oldestCreatedDate));
					oldestCreatedDate = null;
					sumItems = 0;
				}
				currentType = jobletTypeFactory.fromJobletRequest(joblet);
				sumItems = sumItems + joblet.getNumItems();
				if(oldestCreatedDate == null || joblet.getKey().getCreated() < oldestCreatedDate){
					oldestCreatedDate = joblet.getKey().getCreated();
				}
			}
		}
        if(atLeastOnecreatedJoblet){
            summaries.add(new JobletSummary(currentType.getPersistentString(), sumItems, oldestCreatedDate));
        }
		return summaries;
	}

	public static ArrayList<JobletRequest> filterByTypeStatusReservedByPrefix(Iterable<JobletRequest> ins, JobletType<?> type,
			JobletStatus status, String reservedByPrefix){
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

	public static JobletRequest getOldestForTypesAndStatuses(JobletTypeFactory jobletTypeFactory, Iterable<JobletRequest> joblets,
			Collection<JobletType<?>> types, Collection<JobletStatus> statuses){
		JobletRequest oldest = null;
		long now = System.currentTimeMillis();
		for(JobletRequest joblet : DrIterableTool.nullSafe(joblets)){
			JobletType<?> jobletType = jobletTypeFactory.fromJobletRequest(joblet);
			if(types.contains(jobletType) && statuses.contains(joblet.getStatus())){
				if(oldest == null){
					oldest = joblet;
				}
				long ageMs = now - joblet.getKey().getCreated();
				long oldestAgeMs = now - oldest.getKey().getCreated();
				if(ageMs > oldestAgeMs){
					oldest = joblet;
				}
			}
		}
		return oldest;
	}

	public String getCreatedAgo(){
		if(this.getKey().getCreated() == null){
			return "";
		}
		return DrDateTool.getAgoString(this.getKey().getCreated());
	}

	public static List<JobletDataKey> getJobletDataKeys(List<JobletRequest> joblets){
		return StreamTool.stream(joblets)
				.map( joblet -> new JobletDataKey(joblet.getJobletDataId()))
				.collect(Collectors.toList());
	}

    /********************************** getters/setters *******************************/

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

	public Boolean getUnmarshalled(){
		return unmarshalled;
	}

	public void setUnmarshalled(Boolean unmarshalled){
		this.unmarshalled = unmarshalled;
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

	public void setRestartable(Boolean restartable) {
		this.restartable = restartable;
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

	public void setExecutionOrder(Integer executionOrder){
		this.key.setExecutionOrder(executionOrder);
	}

	public String getTypeString(){
		return key.getType();
	}

	public void setType(JobletType<?> type) {
		this.key.setType(type==null?null:type.getPersistentString());
	}

	public void setBatchSequence(Integer batchSequence){
		this.getKey().setBatchSequence(batchSequence);
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

	public boolean isDeleted(){
		return deleted;
	}

	public void setDeleted(boolean deleted){
		this.deleted = deleted;
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

	public void setKey(JobletRequestKey key){
		this.key = key;
	}

	public void setTimer(PhaseTimer timer){
		this.timer = timer;
	}

}