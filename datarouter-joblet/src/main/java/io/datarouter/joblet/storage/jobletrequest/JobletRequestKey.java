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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.Scanner;

public class JobletRequestKey extends BaseRegularPrimaryKey<JobletRequestKey>{

	public static final int DEFAULT_STRING_LENGTH = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	private String type;
	private Integer executionOrder;
	private Long created;//TODO rename createdMs or use Date
	private Integer batchSequence = 0;//tie breaker for keys "created" in same millisecond

	public static class FieldKeys{
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final IntegerFieldKey executionOrder = new IntegerFieldKey("executionOrder");
		public static final LongFieldKey created = new LongFieldKey("created");
		public static final IntegerFieldKey batchSequence = new IntegerFieldKey("batchSequence");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.type, type),
				new IntegerField(FieldKeys.executionOrder, executionOrder),
				new LongField(FieldKeys.created, created),
				new IntegerField(FieldKeys.batchSequence, batchSequence));
	}

	/*----------------------- construct -----------------------*/

	JobletRequestKey(){
	}

	private JobletRequestKey(JobletRequestKey other){
		this.type = other.type;
		this.executionOrder = other.executionOrder;
		this.created = other.created;
		this.batchSequence = other.batchSequence;
	}

	public JobletRequestKey(String type, Integer executionOrder, Long createdMs, Integer batchSequence){
		this.type = type;
		this.executionOrder = executionOrder;
		this.created = createdMs;
		this.batchSequence = batchSequence;
	}

	public JobletRequestKey(JobletType<?> type, JobletPriority jobletPriority, Long createdMs, Integer batchSequence){
		this(type.getPersistentString(), jobletPriority.getExecutionOrder(), createdMs, batchSequence);
	}

	//static method to avoid ambiguity with constructor
	public static JobletRequestKey create(
			JobletType<?> type,
			Integer executionOrder,
			Date createdDate,
			Integer batchSequence){
		return new JobletRequestKey(type == null ? null : type.getPersistentString(),
				executionOrder,
				createdDate == null ? null : createdDate.getTime(),
				batchSequence);
	}

	public static Scanner<JobletRequestKey> createPrefixesForTypesAndPriorities(
			Collection<JobletType<?>> types,
			Collection<JobletPriority> priorities){
		return Scanner.of(types)
				.concat(type -> Scanner.of(priorities)
						.map(priority -> create(type, priority.getExecutionOrder(), null, null)));
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

	public JobletRequestKey withCreated(Long created){
		this.created = created;
		return this;
	}

	public Integer getExecutionOrder(){
		return executionOrder;
	}

	public Integer getBatchSequence(){
		return batchSequence;
	}

	public String getType(){
		return type;
	}

}
