package com.hotpads.joblet.dto;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.joblet.databean.JobletRequest;


public class JobletSummary{
	private static final Logger logger = LoggerFactory.getLogger(JobletSummary.class);

	//key fields
	private Integer executionOrder;
	private String status;
	private String typeString;
	private String queueId;
	//summary fields
	//TODO use primitives
	private Integer numFailures = 0;
	private Integer numType = 0;
	private Integer sumItems = 0;
	private Float avgItems = 0F;//TODO delete and calculate on the fly
	private Integer sumTasks = 0;
	private Float avgTasks = 0F;//TODO delete and calculate on the fly
	private Date firstCreated;
	private Date firstReserved;
	//for jsp
	private boolean expandable = false;

	public JobletSummary(JobletRequest request){
		this.typeString = request.getTypeString();
		this.executionOrder = request.getKey().getExecutionOrder();
		this.queueId = request.getQueueId();
	}

	public JobletSummary(String typeString, Integer sumItems, Long created){
		this.typeString = typeString;
		this.sumItems = sumItems;
		if(created != null){
			this.firstCreated = new Date(created);
		}
	}

	@Deprecated
	public JobletSummary(Object[] cols){
		try{
			this.executionOrder = cols[0]==null?null:Integer.valueOf(cols[0].toString());
			this.status = cols[1]==null?null:cols[1].toString();
			this.typeString = cols[2]==null?null: cols[2].toString();
			this.numFailures = cols[3]==null?null:Integer.valueOf(cols[3].toString());
			this.numType = cols[4]==null?null:Integer.valueOf(cols[4].toString());
			this.sumItems = cols[5]==null?null:Integer.valueOf(cols[5].toString());
			this.avgItems = cols[6]==null?null:Float.valueOf(cols[6].toString());
			this.sumTasks = cols[7]==null?null:Integer.valueOf(cols[7].toString());
			this.avgTasks = cols[8]==null?null:Float.valueOf(cols[8].toString());
			this.firstCreated = cols[9]==null?null:new Date(((BigInteger)cols[9]).longValue());
			this.firstReserved = cols[10]==null?null:new Date(((BigInteger)cols[10]).longValue());
			if(cols.length > 11){
				this.queueId = cols[11]==null?null:cols[11].toString();
			}
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	/*------------------------ static ---------------------------*/

	//group by queueId where all types and executionOrders are the same
	public static Map<String,JobletSummary> buildQueueSummaries(Stream<JobletRequest> requests){
		Map<String,JobletSummary> summaryByQueueId = new TreeMap<>();
		requests.forEach(request -> {
			logger.warn("adding {}", request.getKey());
			summaryByQueueId.putIfAbsent(request.getQueueId(), new JobletSummary(request));
			JobletSummary summary = summaryByQueueId.get(request.getQueueId());
			Preconditions.checkArgument(summary.equalsTypeExecutionOrderQueueId(request));
			summary.include(request);
		});
		return summaryByQueueId;
	}


	/*------------------------ methods --------------------------*/

	public JobletSummary combineWith(JobletSummary other){
		numFailures += other.getNumFailures();
		numType += other.getNumType();
		sumItems += other.getSumItems();
		avgItems = sumItems / (float)numType;
		sumTasks += other.getSumTasks();
		avgTasks = sumTasks / (float)numType;
		firstCreated = DrComparableTool.getFirst(Arrays.asList(firstCreated, other.getFirstCreated()));
		firstReserved = DrComparableTool.getFirst(Arrays.asList(firstReserved, other.getFirstCreated()));
		return this;
	}

	public JobletSummary include(JobletRequest request){
		Preconditions.checkNotNull(request);
		numFailures += DrNumberTool.nullSafe(request.getNumFailures());
		++numType;
		sumItems += request.getNumItems();
		avgItems = sumItems / (float)numType;
		sumTasks += request.getNumTasks();
		avgTasks = sumTasks / (float)numType;
		if(firstCreated == null || request.getKey().getCreatedDate().compareTo(firstCreated) < 0){
			firstCreated = request.getKey().getCreatedDate();
		}
		if(firstReserved == null || request.getReservedAtDate().compareTo(firstReserved) < 0){
			firstReserved = request.getReservedAtDate();
		}
		return this;
	}

	public boolean equalsTypeExecutionOrderQueueId(JobletRequest request){
		return Objects.equals(typeString, request.getTypeString())
				&& Objects.equals(executionOrder, request.getKey().getExecutionOrder())
				&& Objects.equals(queueId, request.getQueueId());
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


	/*----------------------- Object ----------------------*/

	@Override
	public String toString() {
		return "JobletSummary [avgItems=" + avgItems + ", avgTasks=" + avgTasks + ", executionOrder=" + executionOrder
				+ ", firstCreated=" + firstCreated + ", firstReserved=" + firstReserved + ", numFailures=" + numFailures
				+ ", numType=" + numType + ", queueId=" + queueId + ", status=" + status + ", sumItems=" + sumItems
				+ ", sumTasks=" + sumTasks + ", type=" + typeString + "]";
	}


	/*-------------------- get/set --------------------*/

	public Integer getExecutionOrder() {
		return executionOrder;
	}

	public void setExeuctionOrder(Integer executionOrder){
		this.executionOrder = executionOrder;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public void setNumFailures(Integer numFailures){
		this.numFailures = numFailures;
	}

	public Integer getNumType() {
		return numType;
	}

	public void setNumType(Integer numType) {
		this.numType = numType;
	}

	public Integer getSumItems() {
		return sumItems;
	}

	public void setSumItems(Integer sumItems) {
		this.sumItems = sumItems;
	}

	public Float getAvgItems() {
		return avgItems;
	}

	public void setAvgItems(Float avgItems) {
		this.avgItems = avgItems;
	}

	public Integer getSumTasks() {
		return sumTasks;
	}

	public void setSumTasks(Integer sumTasks) {
		this.sumTasks = sumTasks;
	}

	public Float getAvgTasks() {
		return avgTasks;
	}

	public void setAvgTasks(Float avgTasks) {
		this.avgTasks = avgTasks;
	}

	public Date getFirstCreated() {
		return firstCreated;
	}

	public void setFirstCreated(Date firstCreated) {
		this.firstCreated = firstCreated;
	}

	public Date getFirstReserved() {
		return firstReserved;
	}

	public void setFirstReserved(Date firstReserved) {
		this.firstReserved = firstReserved;
	}

	public void setQueueId(String queueId){
		this.queueId = queueId;
	}

	public boolean getExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}
}