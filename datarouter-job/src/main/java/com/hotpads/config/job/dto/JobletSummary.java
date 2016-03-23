package com.hotpads.config.job.dto;

import java.math.BigInteger;
import java.util.Date;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberTool;

/*************** inner class ********************************/

public class JobletSummary{
	public Integer executionOrder;
	public String status;
	public String typeString;
	public String queueId;
	public Integer numFailures;
	public Integer numType;
	public Integer sumItems;
	public Float avgItems;
	public Integer sumTasks;
	public Float avgTasks;
	public Date firstCreated;
	public Date firstReserved;
	public boolean expandable = false;

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

	public boolean isEmpty(){
		return DrNumberTool.isEmpty(numType);
	}

	public String getFirstCreatedAgo(){
		if(this.firstCreated==null){ return ""; }
		return DrDateTool.getAgoString(this.firstCreated);
	}

	public String getFirstReservedAgo(){
		if(this.firstReserved==null){ return ""; }
		return DrDateTool.getAgoString(this.firstReserved);
	}

	@Override
	public String toString() {
		return "JobletSummary [avgItems=" + avgItems + ", avgTasks="
				+ avgTasks + ", executionOrder=" + executionOrder
				+ ", firstCreated=" + firstCreated + ", firstReserved="
				+ firstReserved + ", numFailures=" + numFailures
				+ ", numType=" + numType + ", queueId=" + queueId
				+ ", status=" + status + ", sumItems=" + sumItems
				+ ", sumTasks=" + sumTasks + ", type=" + typeString + "]";
	}


	/****************************** get/set **********************************/



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