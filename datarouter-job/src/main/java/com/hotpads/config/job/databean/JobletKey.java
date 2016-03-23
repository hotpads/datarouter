package com.hotpads.config.job.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import com.hotpads.config.job.enums.JobletType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;


@SuppressWarnings("serial")
@Entity
@Embeddable
public class JobletKey extends BasePrimaryKey<JobletKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	@Column(length=DEFAULT_STRING_LENGTH)
	private String type;
	private Integer executionOrder = Integer.MAX_VALUE;
	@Column(length=50)
	private Long created;
	private Integer batchSequence = 0;//tie breaker for keys "created" in same millisecond


	/****************************** constructor ********************************/

	JobletKey(){
	}

	public JobletKey(JobletType<?> type, Integer executionOrder, Integer batchSequence){
		this(type, executionOrder, new Date(), batchSequence);
	}

	public JobletKey(JobletType<?> type, Integer executionOrder, Date created, Integer batchSequence){
		this.type = type==null?null:type.getVarName();
		this.executionOrder = executionOrder;
		this.created = created == null ? null : created.getTime();
		this.batchSequence = batchSequence;
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(
				new StringField(Joblet.F.type, this.type,DEFAULT_STRING_LENGTH),
				new IntegerField(Joblet.F.executionOrder, this.executionOrder),
				new LongField(Joblet.F.created, this.created),
				new IntegerField(Joblet.F.batchSequence, this.batchSequence));
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
