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


public class JobletSummary implements Comparable<JobletSummary>{

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
		Map<JobletSummary,JobletSummary> summaries = new TreeMap<>();//hack to emulate Set::get
		requests.map(JobletSummary::new).forEach(summary -> {
			summaries.putIfAbsent(summary, summary);
			summaries.get(summary).include(summary);
		});
		return new ArrayList<>(summaries.values());
	}

	//group by queueId where all types and executionOrders are the same
	public static Map<ComparablePair<String,JobletStatus>,JobletSummary> buildQueueSummaries(
			Stream<JobletRequest> requests){
		Map<ComparablePair<String,JobletStatus>,JobletSummary> summaryByQueueIdStatus = new TreeMap<>();
		requests.forEach(request -> {
			ComparablePair<String,JobletStatus> queueIdStatus = new ComparablePair<>(request.getQueueId(), request
					.getStatus());
			JobletSummary summary = summaryByQueueIdStatus.computeIfAbsent(queueIdStatus, a -> new JobletSummary(
					request));
			Preconditions.checkArgument(summary.equalsTypeExecutionOrderQueueIdStatus(request));
			summary.include(request);
		});
		return summaryByQueueIdStatus;
	}


	/*------------------------ methods --------------------------*/

	public JobletSummary include(JobletSummary other){
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

	public JobletSummary include(JobletRequest request){
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

	public boolean equalsTypeExecutionOrderStatus(JobletRequest request){
		return request != null
				&& Objects.equals(typeString, request.getTypeString())
				&& Objects.equals(executionOrder, request.getKey().getExecutionOrder())
				&& Objects.equals(status, request.getStatus());
	}

	public boolean differentTypeExecutionOrder(JobletRequest request){
		return !equalsTypeExecutionOrderStatus(request);
	}

	public boolean equalsTypeExecutionOrderQueueIdStatus(JobletRequest request){
		return request != null
				&& Objects.equals(typeString, request.getTypeString())
				&& Objects.equals(executionOrder, request.getKey().getExecutionOrder())
				&& Objects.equals(queueId, request.getQueueId())
				&& Objects.equals(status, request.getStatus());
	}

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
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executionOrder == null) ? 0 : executionOrder.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((typeString == null) ? 0 : typeString.hashCode());
		return result;
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
	public String toString(){
		return "JobletSummary [executionOrder=" + executionOrder + ", status=" + status + ", typeString=" + typeString
				+ ", queueId=" + queueId + ", queueIds=" + queueIds + ", numFailures=" + numFailures + ", numType="
				+ numType + ", sumItems=" + sumItems + ", sumTasks=" + sumTasks + ", firstCreated=" + firstCreated
				+ ", firstReserved=" + firstReserved + "]";
	}

	/*------------------ Comparable -------------------*/

	@Override
	public int compareTo(JobletSummary other){
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