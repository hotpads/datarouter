package com.hotpads.joblet.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.joblet.enums.JobletType;


@SuppressWarnings("serial")
@Entity
@Embeddable
public class JobletRequestKey extends BasePrimaryKey<JobletRequestKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	@Column(length=DEFAULT_STRING_LENGTH)
	private String type;
	private Integer executionOrder = Integer.MAX_VALUE;
	@Column(length=50)
	private Long created;
	private Integer batchSequence = 0;//tie breaker for keys "created" in same millisecond


	/****************************** constructor ********************************/

	JobletRequestKey(){
	}

	public JobletRequestKey(JobletType<?> type, Integer executionOrder, Integer batchSequence){
		this(type, executionOrder, new Date(), batchSequence);
	}

	public JobletRequestKey(JobletType<?> type, Integer executionOrder, Date created, Integer batchSequence){
		this.type = type==null?null:type.getPersistentString();
		this.executionOrder = executionOrder;
		this.created = created == null ? null : created.getTime();
		this.batchSequence = batchSequence;
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(
				new StringField(JobletRequest.F.type, this.type,DEFAULT_STRING_LENGTH),
				new IntegerField(JobletRequest.F.executionOrder, this.executionOrder),
				new LongField(JobletRequest.F.created, this.created),
				new IntegerField(JobletRequest.F.batchSequence, this.batchSequence));
	}


	/*************************** get/set **********************************/

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