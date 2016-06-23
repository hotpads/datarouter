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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.util.core.collections.ComparablePair;


public class JobletSummary{
	private static final Logger logger = LoggerFactory.getLogger(JobletSummary.class);

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
		List<JobletSummary> summaries = new ArrayList<>();
		requests.forEach(request -> {
			JobletSummary previous = DrCollectionTool.getLast(summaries);
			if(previous != null && previous.equalsTypeExecutionOrderStatus(request)){
				previous.include(request);
			}else{
				summaries.add(new JobletSummary(request));
			}
		});
		return summaries;
	}

	//group by queueId where all types and executionOrders are the same
	public static Map<ComparablePair<String,JobletStatus>,JobletSummary> buildQueueSummaries(Stream<JobletRequest> requests){
		Map<ComparablePair<String,JobletStatus>,JobletSummary> summaryByQueueIdStatus = new TreeMap<>();
		requests.forEach(request -> {
			ComparablePair<String,JobletStatus> queueIdStatus = new ComparablePair<>(request.getQueueId(), request
					.getStatus());
			summaryByQueueIdStatus.putIfAbsent(queueIdStatus, new JobletSummary(request));
			JobletSummary summary = summaryByQueueIdStatus.get(queueIdStatus);
			Preconditions.checkArgument(summary.equalsTypeExecutionOrderQueueIdStatus(request));
			summary.include(request);
		});
		return summaryByQueueIdStatus;
	}


	/*------------------------ methods --------------------------*/

	public JobletSummary include(JobletRequest request){
		Preconditions.checkNotNull(request);
		queueIds.add(request.getQueueId());
		numFailures += DrNumberTool.nullSafe(request.getNumFailures());
		++numType;
		sumItems += request.getNumItems();
		sumTasks += request.getNumTasks();
		if(firstCreated == null || request.getKey().getCreatedDate().compareTo(firstCreated) < 0){
			firstCreated = request.getKey().getCreatedDate();
		}
		if(firstReserved == null || request.getReservedAtDate().compareTo(firstReserved) < 0){
			firstReserved = request.getReservedAtDate();
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
	public String toString(){
		return "JobletSummary [executionOrder=" + executionOrder + ", status=" + status + ", typeString=" + typeString
				+ ", queueId=" + queueId + ", queueIds=" + queueIds + ", numFailures=" + numFailures + ", numType="
				+ numType + ", sumItems=" + sumItems + ", sumTasks=" + sumTasks + ", firstCreated=" + firstCreated
				+ ", firstReserved=" + firstReserved + "]";
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