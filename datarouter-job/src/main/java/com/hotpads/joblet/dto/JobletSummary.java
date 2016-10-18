package com.hotpads.joblet.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.util.core.lang.ClassTool;


public class JobletSummary{

	//key fields
	private Integer executionOrder;
	private JobletStatus status;
	private String typeString;
	private Integer typeCode;
	private String queueId;
	//summary fields
	private Set<String> queueIds = new HashSet<>();
	private int numFailures;
	private int numType;
	private int sumItems;
	private int sumTasks;
	private Date firstCreated;
	private Date firstReserved;

	public JobletSummary(JobletRequest request){
		this.typeCode = request.getKey().getTypeCode();
		this.typeString = request.getTypeString();
		this.executionOrder = request.getKey().getExecutionOrder();
		this.status = request.getStatus();
		this.queueId = request.getQueueId();
		absorbJobletRequestStats(request);
	}

	/*------------------------ static ---------------------------*/

	public static Map<TypeKey,JobletSummary> summarizeByType(Stream<JobletRequest> requests){
		return requests
				.map(JobletSummary::new)
				.collect(Collectors.toMap(
						TypeKey::new,
						Function.identity(),
						JobletSummary::absorbStats,
						TreeMap::new));
	}

	public static Map<TypeExecutionOrderStatusKey,JobletSummary> summarizeByTypeExecutionOrderStatus(
			Stream<JobletRequest> requests){
		return requests
				.map(JobletSummary::new)
				.collect(Collectors.toMap(
						TypeExecutionOrderStatusKey::new,
						Function.identity(),
						JobletSummary::absorbStats,
						TreeMap::new));
	}

	public static Map<QueueStatusKey,JobletSummary> summarizeByQueueStatus(Stream<JobletRequest> requests){
		return requests
				.map(JobletSummary::new)
				.collect(Collectors.toMap(
						QueueStatusKey::new,
						Function.identity(),
						JobletSummary::absorbStats,
						TreeMap::new));
	}


	/*------------------------ private --------------------------*/

	private JobletSummary absorbStats(JobletSummary other){
		Preconditions.checkNotNull(other);
		if(DrStringTool.notEmpty(other.queueId)){
			queueIds.add(other.queueId);
		}
		numFailures += DrNumberTool.nullSafe(other.numFailures);
		++numType;
		sumItems += other.sumItems;
		sumTasks += other.sumTasks;
		if(firstCreated == null || other.firstCreated.before(firstCreated)){
			firstCreated = other.firstCreated;
		}
		if(other.firstReserved != null){
			if(firstReserved == null || other.firstReserved.before(firstReserved)){
				firstReserved = other.firstReserved;
			}
		}
		return this;
	}

	private JobletSummary absorbJobletRequestStats(JobletRequest request){
		Preconditions.checkNotNull(request);
		if(DrStringTool.notEmpty(request.getQueueId())){
			queueIds.add(request.getQueueId());
		}
		numFailures += DrNumberTool.nullSafe(request.getNumFailures());
		++numType;
		sumItems += request.getNumItems();
		sumTasks += request.getNumTasks();
		if(firstCreated == null || request.getKey().getCreatedDate().compareTo(firstCreated) < 0){
			firstCreated = request.getKey().getCreatedDate();
		}
		if(request.getReservedAtDate() != null){
			if(firstReserved == null || request.getReservedAtDate().compareTo(firstReserved) < 0){
				firstReserved = request.getReservedAtDate();
			}
		}
		return this;
	}


	/*------------------------ public (jsp) --------------------------*/

	public boolean isEmpty(){
		return DrNumberTool.isEmpty(numType);
	}

	public String getFirstCreatedAgo(){
		if(this.firstCreated == null) {
			return "";
		}
		return DrDateTool.getAgoString(this.firstCreated);
	}

	public String getFirstReservedAgo(){
		if(this.firstReserved == null) {
			return "";
		}
		return DrDateTool.getAgoString(this.firstReserved);
	}

	public int getNumQueueIds(){
		return queueIds.size();
	}

	public double getAvgItems(){
		return (double)sumItems / (double)numType;
	}

	public double getAvgTasks(){
		return (double)sumItems / (double)numType;
	}


	/*----------------------- Object ----------------------*/

	@Override
	public String toString(){
		return "JobletSummary [executionOrder=" + executionOrder + ", status=" + status + ", typeString=" + typeString
				+ ", queueId=" + queueId + ", queueIds=" + queueIds + ", numFailures=" + numFailures + ", numType="
				+ numType + ", sumItems=" + sumItems + ", sumTasks=" + sumTasks + ", firstCreated=" + firstCreated
				+ ", firstReserved=" + firstReserved + "]";
	}


	/*------------------ keys -----------------------*/

	private static class TypeKey implements Comparable<TypeKey>{
		private final int typeCode;

		public TypeKey(JobletSummary summary){
			this.typeCode = summary.typeCode;
		}

		@Override
		public boolean equals(Object obj){
			if(ClassTool.differentClass(this, obj)){
				return false;
			}
			JobletSummary other = (JobletSummary)obj;
			return Objects.equals(typeCode, other.typeCode);
		}

		@Override
		public int hashCode(){
			return Objects.hash(typeCode);
		}

		@Override
		public int compareTo(TypeKey other){
			return DrComparableTool.nullFirstCompareTo(typeCode, other.typeCode);
		}
	}

	private static class TypeExecutionOrderStatusKey implements Comparable<TypeExecutionOrderStatusKey>{
		private final int typeCode;
		private final Integer executionOrder;
		private final JobletStatus status;

		public TypeExecutionOrderStatusKey(JobletSummary summary){
			this.typeCode = summary.typeCode;
			this.executionOrder = summary.executionOrder;
			this.status = summary.status;
		}

		@Override
		public boolean equals(Object obj){
			if(ClassTool.differentClass(this, obj)){
				return false;
			}
			JobletSummary other = (JobletSummary)obj;
			return Objects.equals(typeCode, other.typeCode)
					&& Objects.equals(executionOrder, other.executionOrder)
					&& Objects.equals(status, other.status);
		}

		@Override
		public int hashCode(){
			return Objects.hash(typeCode, executionOrder, status);
		}

		@Override
		public int compareTo(TypeExecutionOrderStatusKey other){
			int diff = DrComparableTool.nullFirstCompareTo(typeCode, other.typeCode);
			if(diff != 0){
				return diff;
			}
			diff = DrComparableTool.nullFirstCompareTo(executionOrder, other.executionOrder);
			if(diff != 0){
				return diff;
			}
			return DrComparableTool.nullFirstCompareTo(status, other.status);
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
			JobletSummary other = (JobletSummary)obj;
			return Objects.equals(queueId, other.queueId)
					&& Objects.equals(status, other.status);
		}

		@Override
		public int hashCode(){
			return Objects.hash(queueId, status);
		}

		@Override
		public int compareTo(QueueStatusKey other){
			int diff = DrComparableTool.nullFirstCompareTo(queueId, other.queueId);
			if(diff != 0){
				return diff;
			}
			return DrComparableTool.nullFirstCompareTo(status, other.status);
		}
	}

	/*-------------------- get/set --------------------*/

	public Integer getExecutionOrder() {
		return executionOrder;
	}

	public JobletStatus getStatus() {
		return status;
	}

	public Integer getTypeCode(){
		return typeCode;
	}

	public String getTypeString() {
		return typeString;
	}

	public String getQueueId() {
		return queueId;
	}

	public Integer getNumFailures() {
		return numFailures;
	}

	public Integer getNumType() {
		return numType;
	}

	public Integer getSumItems() {
		return sumItems;
	}

	public Integer getSumTasks() {
		return sumTasks;
	}

	public Date getFirstCreated() {
		return firstCreated;
	}

	public Date getFirstReserved() {
		return firstReserved;
	}

}