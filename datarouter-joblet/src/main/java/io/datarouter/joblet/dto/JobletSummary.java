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
package io.datarouter.joblet.dto;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.WarnOnModifyList;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.DateTool;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;

public class JobletSummary{

	//key fields
	private String type;
	private Integer executionOrder;
	private JobletStatus status;
	private String queueId;
	//summary fields
	private Set<String> queueIds = new HashSet<>();
	private long numFailures;
	private long numType;
	private long sumItems;
	private Instant firstCreated;
	private Instant firstReserved;

	public JobletSummary(JobletRequest request){
		this.type = request.getKey().getType();
		this.executionOrder = request.getKey().getExecutionOrder();
		this.status = request.getStatus();
		this.queueId = request.getQueueId();
		absorbJobletRequestStats(request);
	}

	/*------------------------ static ---------------------------*/

	public static List<JobletSummary> summarizeByTypeExecutionOrderStatus(
			Scanner<JobletRequest> requests){
		return requests
				.map(JobletSummary::new)
				.toMapSupplied(TypeExecutionOrderStatusKey::new,
						Function.identity(),
						JobletSummary::absorbStats,
						TreeMap::new)
				.values().stream()
				.sorted(Comparator.comparing(JobletSummary::getType)
						.thenComparing(JobletSummary::getExecutionOrder)
						.thenComparing(JobletSummary::getStatus))
				.collect(WarnOnModifyList.deprecatedCollector());
	}

	public static Map<QueueStatusKey,JobletSummary> summarizeByQueueStatus(Scanner<JobletRequest> requests){
		return requests
				.map(JobletSummary::new)
				.toMapSupplied(QueueStatusKey::new,
						Function.identity(),
						JobletSummary::absorbStats,
						TreeMap::new);
	}

	/*------------------------ private --------------------------*/

	public JobletSummary absorbStats(JobletSummary other){
		Objects.requireNonNull(other);
		if(StringTool.notEmpty(other.queueId)){
			queueIds.add(other.queueId);
		}
		numFailures += NumberTool.nullSafeLong(other.numFailures, 0L);
		numType += other.numType;
		sumItems += other.sumItems;
		if(firstCreated == null || other.firstCreated.isBefore(firstCreated)){
			firstCreated = other.firstCreated;
		}
		if(other.firstReserved != null){
			if(firstReserved == null || other.firstReserved.isBefore(firstReserved)){
				firstReserved = other.firstReserved;
			}
		}
		return this;
	}

	private JobletSummary absorbJobletRequestStats(JobletRequest request){
		Objects.requireNonNull(request);
		if(StringTool.notEmpty(request.getQueueId())){
			queueIds.add(request.getQueueId());
		}
		numFailures += NumberTool.nullSafe(request.getNumFailures());
		++numType;
		sumItems += request.getNumItems();
		if(firstCreated == null || request.getKey().getCreatedInstant().isBefore(firstCreated)){
			firstCreated = request.getKey().getCreatedInstant();
		}
		if(request.getReservedAtInstant() != null){
			if(firstReserved == null || request.getReservedAtInstant().isBefore(firstReserved)){
				firstReserved = request.getReservedAtInstant();
			}
		}
		return this;
	}

	/*------------------------ public --------------------------*/

	public boolean isEmpty(){
		return NumberTool.isEmpty(numType);
	}

	public String getFirstCreatedAgo(){
		if(this.firstCreated == null){
			return "";
		}
		return DateTool.getAgoString(this.firstCreated);
	}

	public long getFirstCreatedMsAgo(){
		return firstCreated == null ? -1 : firstCreated.toEpochMilli();
	}

	public String getFirstReservedAgo(){
		if(this.firstReserved == null){
			return "";
		}
		return DateTool.getAgoString(this.firstReserved);
	}

	public long getFirstReservedMsAgo(){
		return firstReserved == null ? -1 : firstReserved.toEpochMilli();
	}

	public int getNumQueueIds(){
		return queueIds.size();
	}

	public double getAvgItems(){
		return (double)sumItems / (double)numType;
	}

	/*----------------------- Object ----------------------*/

	@Override
	public String toString(){
		return "JobletSummary [executionOrder=" + executionOrder + ", status=" + status + ", type=" + type
				+ ", queueId=" + queueId + ", queueIds=" + queueIds + ", numFailures=" + numFailures + ", numType="
				+ numType + ", sumItems=" + sumItems + ", firstCreated=" + firstCreated
				+ ", firstReserved=" + firstReserved + "]";
	}

	/*------------------ keys -----------------------*/

	private static class TypeExecutionOrderStatusKey implements Comparable<TypeExecutionOrderStatusKey>{
		private final String type;
		private final Integer executionOrder;
		private final JobletStatus status;

		public TypeExecutionOrderStatusKey(JobletSummary summary){
			this.type = summary.type;
			this.executionOrder = summary.executionOrder;
			this.status = summary.status;
		}

		@Override
		public boolean equals(Object obj){
			if(ClassTool.differentClass(this, obj)){
				return false;
			}
			TypeExecutionOrderStatusKey other = (TypeExecutionOrderStatusKey)obj;
			return Objects.equals(type, other.type)
					&& Objects.equals(executionOrder, other.executionOrder)
					&& Objects.equals(status, other.status);
		}

		@Override
		public int hashCode(){
			return Objects.hash(type, executionOrder, status);
		}

		@Override
		public int compareTo(TypeExecutionOrderStatusKey other){
			int diff = ComparableTool.nullFirstCompareTo(type, other.type);
			if(diff != 0){
				return diff;
			}
			diff = ComparableTool.nullFirstCompareTo(executionOrder, other.executionOrder);
			if(diff != 0){
				return diff;
			}
			return ComparableTool.nullFirstCompareTo(status, other.status);
		}
	}

	private static class QueueStatusKey implements Comparable<QueueStatusKey>{
		private final String queueId;
		private final JobletStatus status;

		public QueueStatusKey(JobletSummary summary){
			this.queueId = summary.queueId;
			this.status = summary.status;
		}

		@Override
		public boolean equals(Object obj){
			if(ClassTool.differentClass(this, obj)){
				return false;
			}
			QueueStatusKey other = (QueueStatusKey)obj;
			return Objects.equals(queueId, other.queueId)
					&& Objects.equals(status, other.status);
		}

		@Override
		public int hashCode(){
			return Objects.hash(queueId, status);
		}

		@Override
		public int compareTo(QueueStatusKey other){
			int diff = ComparableTool.nullFirstCompareTo(queueId, other.queueId);
			if(diff != 0){
				return diff;
			}
			return ComparableTool.nullFirstCompareTo(status, other.status);
		}
	}

	/*-------------------- get/set --------------------*/

	public Integer getExecutionOrder(){
		return executionOrder;
	}

	public JobletStatus getStatus(){
		return status;
	}

	public String getType(){
		return type;
	}

	public String getQueueId(){
		return queueId;
	}

	public Long getNumFailures(){
		return numFailures;
	}

	public Long getNumType(){
		return numType;
	}

	public Long getSumItems(){
		return sumItems;
	}

	public Instant getFirstCreated(){
		return firstCreated;
	}

}
