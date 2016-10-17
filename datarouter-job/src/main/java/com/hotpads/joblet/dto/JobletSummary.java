package com.hotpads.joblet.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.util.core.collections.ComparablePair;
import com.hotpads.util.core.lang.ClassTool;


public class JobletSummary{

	//key fields
	private Integer executionOrder;
	private JobletStatus status;
	private String typeString;
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
		this.typeString = request.getTypeString();
		this.executionOrder = request.getKey().getExecutionOrder();
		this.status = request.getStatus();
		this.queueId = request.getQueueId();
		absorbJobletRequestStats(request);
	}

	public JobletSummary(String typeString, Integer sumItems, Long created){
		this.typeString = typeString;
		this.sumItems = sumItems;
		if(created != null){
			this.firstCreated = new Date(created);
		}
	}

	/*------------------------ static ---------------------------*/

	public static List<JobletSummary> buildSummaries(Stream<JobletRequest> requests){
		Map<TypeExecutionOrderStatusKey,JobletSummary> summaries = new TreeMap<>();
		requests.map(JobletSummary::new).forEach(summary -> {
			TypeExecutionOrderStatusKey key = new TypeExecutionOrderStatusKey(summary);
			if(summaries.containsKey(key)){
				summaries.get(key).absorbStats(summary);
			}else{
				summaries.put(key, summary);
			}
		});
		return new ArrayList<>(summaries.values());
	}

	//group by queueId where all types and executionOrders are the same
	public static Map<ComparablePair<String,JobletStatus>,JobletSummary> buildQueueSummaries(
			Stream<JobletRequest> requests){
		Map<ComparablePair<String,JobletStatus>,JobletSummary> summaryByQueueIdStatus = new TreeMap<>();
		requests.map(JobletSummary::new).forEach(summary -> {
			ComparablePair<String,JobletStatus> key = new ComparablePair<>(summary.getQueueId(), summary.getStatus());
			if(summaryByQueueIdStatus.containsKey(key)){
				summaryByQueueIdStatus.get(key).absorbStats(summary);
			}else{
				summaryByQueueIdStatus.put(key, summary);
			}
		});
		return summaryByQueueIdStatus;
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

	private static class TypeExecutionOrderStatusKey implements Comparable<TypeExecutionOrderStatusKey>{
		private String typeString;
		private Integer executionOrder;
		private JobletStatus status;

		public TypeExecutionOrderStatusKey(JobletSummary summary){
			this.typeString = summary.typeString;
			this.executionOrder = summary.executionOrder;
			this.status = summary.status;
		}

		@Override
		public boolean equals(Object obj){
			if(ClassTool.differentClass(this, obj)){
				return false;
			}
			JobletSummary other = (JobletSummary)obj;
			return Objects.equals(typeString, other.typeString)
					&& Objects.equals(executionOrder, other.executionOrder)
					&& Objects.equals(status, other.status);
		}

		@Override
		public int hashCode(){
			return Objects.hash(typeString, executionOrder, status);
		}

		@Override
		public int compareTo(TypeExecutionOrderStatusKey other){
			int diff = DrComparableTool.nullFirstCompareTo(typeString, other.typeString);
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

	/*-------------------- get/set --------------------*/

	public Integer getExecutionOrder() {
		return executionOrder;
	}

	public JobletStatus getStatus() {
		return status;
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