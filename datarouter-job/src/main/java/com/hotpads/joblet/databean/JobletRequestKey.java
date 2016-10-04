package com.hotpads.joblet.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletType;


public class JobletRequestKey extends BasePrimaryKey<JobletRequestKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private String type;//TODO use StringEnumField<JobletType<?>>
	private Integer executionOrder = Integer.MAX_VALUE;
	private Long created;//TODO rename createdMs or use Date
	private Integer batchSequence = 0;//tie breaker for keys "created" in same millisecond

	public static class FieldKeys{
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final IntegerFieldKey executionOrder = new IntegerFieldKey("executionOrder");
		public static final LongFieldKey created = new LongFieldKey("created");
		public static final IntegerFieldKey batchSequence = new IntegerFieldKey("batchSequence");
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(
				new StringField(FieldKeys.type, type),
				new IntegerField(FieldKeys.executionOrder, executionOrder),
				new LongField(FieldKeys.created, created),
				new IntegerField(FieldKeys.batchSequence, batchSequence));
	}

	/*----------------------- construct -----------------------*/

	JobletRequestKey(){
	}

	//static method to avoid ambiguity with below constructor
	public static JobletRequestKey create(JobletType<?> type, Integer executionOrder, Date createdDate,
			Integer batchSequence){
		return new JobletRequestKey(type == null ? null : type.getPersistentString(),
				executionOrder,
				createdDate == null ? null : createdDate.getTime(),
				batchSequence);
	}

	public JobletRequestKey(String typeString, Integer executionOrder, Long createdMs, Integer batchSequence){
		this.type = typeString;
		this.executionOrder = executionOrder;
		this.created = createdMs;
		this.batchSequence = batchSequence;
	}

	/*----------------------- methods ---------------------------*/

	public JobletPriority getPriority(){
		return JobletPriority.fromExecutionOrder(executionOrder);
	}

	public Date getCreatedDate(){
		return new Date(created);
	}

	/*----------------------- get/set -----------------------*/

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Integer getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(Integer executionOrder) {
		this.executionOrder = executionOrder;
	}

	public Integer getBatchSequence() {
		return batchSequence;
	}

	public void setBatchSequence(Integer batchSequence) {
		this.batchSequence = batchSequence;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
