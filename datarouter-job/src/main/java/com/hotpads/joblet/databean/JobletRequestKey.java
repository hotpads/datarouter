package com.hotpads.joblet.databean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.type.JobletType;


public class JobletRequestKey extends BasePrimaryKey<JobletRequestKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.DEFAULT_LENGTH_VARCHAR;

	private Integer typeCode;
	private Integer executionOrder = Integer.MAX_VALUE;
	private Long created;//TODO rename createdMs or use Date
	private Integer batchSequence = 0;//tie breaker for keys "created" in same millisecond

	public static class FieldKeys{
		public static final IntegerFieldKey typeCode = new IntegerFieldKey("typeCode");
		public static final IntegerFieldKey executionOrder = new IntegerFieldKey("executionOrder");
		public static final LongFieldKey created = new LongFieldKey("created");
		public static final IntegerFieldKey batchSequence = new IntegerFieldKey("batchSequence");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new IntegerField(FieldKeys.typeCode, typeCode),
				new IntegerField(FieldKeys.executionOrder, executionOrder),
				new LongField(FieldKeys.created, created),
				new IntegerField(FieldKeys.batchSequence, batchSequence));
	}

	/*----------------------- construct -----------------------*/

	JobletRequestKey(){
	}

	private JobletRequestKey(JobletRequestKey other){
		this.typeCode = other.typeCode;
		this.executionOrder = other.executionOrder;
		this.created = other.created;
		this.batchSequence = other.batchSequence;
	}

	public JobletRequestKey(Integer typeCode, Integer executionOrder, Long createdMs, Integer batchSequence){
		this.typeCode = typeCode;
		this.executionOrder = executionOrder;
		this.created = createdMs;
		this.batchSequence = batchSequence;
	}

	public JobletRequestKey(JobletType<?> type, JobletPriority jobletPriority, Long createdMs, Integer batchSequence){
		this(type.getPersistentInt(), jobletPriority.getExecutionOrder(), createdMs, batchSequence);
	}

	//static method to avoid ambiguity with constructor
	public static JobletRequestKey create(JobletType<?> type, Integer executionOrder, Date createdDate,
			Integer batchSequence){
		return new JobletRequestKey(type == null ? null : type.getPersistentInt(),
				executionOrder,
				createdDate == null ? null : createdDate.getTime(),
				batchSequence);
	}

	public static List<JobletRequestKey> createPrefixesForTypesAndPriorities(Collection<JobletType<?>> types,
			Collection<JobletPriority> priorities){
		List<JobletRequestKey> prefixes = new ArrayList<>();
		for(JobletType<?> type : types){
			for(JobletPriority priority : priorities){
				prefixes.add(create(type, priority.getExecutionOrder(), null, null));
			}
		}
		return prefixes;
	}

	/*----------------------- methods ---------------------------*/

	public JobletRequestKey copy(){
		return new JobletRequestKey(this);
	}

	public JobletPriority getPriority(){
		return JobletPriority.fromExecutionOrder(executionOrder);
	}

	public Date getCreatedDate(){
		return new Date(created);
	}

	public Duration getAge(){
		return Duration.ofMillis(System.currentTimeMillis() - created);
	}

	/*----------------------- get/set -----------------------*/

	public Long getCreated(){
		return created;
	}

	public void setCreated(Long created){
		this.created = created;
	}

	public Integer getExecutionOrder(){
		return executionOrder;
	}

	public void setExecutionOrder(Integer executionOrder){
		this.executionOrder = executionOrder;
	}

	public Integer getBatchSequence(){
		return batchSequence;
	}

	public void setBatchSequence(Integer batchSequence){
		this.batchSequence = batchSequence;
	}

	public Integer getTypeCode(){
		return typeCode;
	}

	public void setTypeCode(Integer typeCode){
		this.typeCode = typeCode;
	}

}
