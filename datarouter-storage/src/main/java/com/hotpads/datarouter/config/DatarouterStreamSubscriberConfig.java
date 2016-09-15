package com.hotpads.datarouter.config;

import java.util.Date;

public class DatarouterStreamSubscriberConfig implements DatarouterStreamSubscriberAccessorSetter{
	private final Integer blockingQueueSize;
	private final Integer maxRecordsPerRequest;

	private Integer initialLeaseTableReadCapacity;
	private Integer initialLeaseTableWriteCapacity;
	private DrInitialPositionInStream drInitialPositionInStream = DrInitialPositionInStream.LATEST;
	private DatarouterStreamSubscriberAccessor subscriberAccessor;
	private Date timestamp;
	private String subscriberAppName;
	private Boolean replayData;

	public DatarouterStreamSubscriberConfig(Integer blockingQueueSize, Integer maxRecordsPerRequest){
		this.blockingQueueSize = blockingQueueSize;
		this.maxRecordsPerRequest = maxRecordsPerRequest;
	}

	@Override
	public void setDatarouterStreamSubscriberAccessor(DatarouterStreamSubscriberAccessor subscriberAccessor){
		this.subscriberAccessor = subscriberAccessor;
	}

	public DatarouterStreamSubscriberConfig withInitialPositionInStream(
			DrInitialPositionInStream initialPositionInStream){
		this.drInitialPositionInStream = initialPositionInStream;
		return this;
	}

	public DatarouterStreamSubscriberConfig withTimestampAtInitialPositionInStreamAtTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		this.drInitialPositionInStream = DrInitialPositionInStream.AT_TIMESTAMP;
		return this;
	}

	public DatarouterStreamSubscriberConfig withExplicitSubscriberAppName(String subscriberAppName){
		this.subscriberAppName = subscriberAppName;
		return this;
	}

	public DatarouterStreamSubscriberConfig withReplayData(Boolean replayData){
		this.replayData = replayData;
		return this;
	}

	public DatarouterStreamSubscriberConfig withInitialLeaseTableReadCapacity(Integer initialLeaseTableReadCapacity){
		this.initialLeaseTableReadCapacity = initialLeaseTableReadCapacity;
		return this;
	}

	public DatarouterStreamSubscriberConfig withInitialLeaseTableWriteCapacity(Integer initialLeaseTableWriteCapacity){
		this.initialLeaseTableWriteCapacity = initialLeaseTableWriteCapacity;
		return this;
	}

	public DatarouterStreamSubscriberAccessor getSubscriberAccessor(){
		return subscriberAccessor;
	}

	public Date getTimestamp(){
		return timestamp;
	}

	public int getBlockingQueueSize(){
		return blockingQueueSize;
	}

	public Integer getMaxRecordsPerRequest(){
		return maxRecordsPerRequest;
	}

	public String getSubscriberAppName(){
		return subscriberAppName;
	}

	public DrInitialPositionInStream getDrInitialPositionInStream(){
		return drInitialPositionInStream;
	}

	public Boolean getReplayData(){
		return replayData;
	}

	public Integer getInitialLeaseTableReadCapacity(){
		return initialLeaseTableReadCapacity;
	}

	public Integer getInitialLeaseTableWriteCapacity(){
		return initialLeaseTableWriteCapacity;
	}
}
