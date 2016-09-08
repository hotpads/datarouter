package com.hotpads.datarouter.config;

import java.util.Date;

public class DatarouterStreamSubscriberConfig implements DatarouterStreamSubscriberAccessorSetter{
	private DatarouterStreamSubscriberAccessor subscriberAccessor;
	private Date timestamp;
	private DrInitialPositionInStream drInitialPositionInStream;
	private String subscriberAppName;
	private int blockingQueueSize;

	public DatarouterStreamSubscriberConfig(int blockingQueueSize, DrInitialPositionInStream drInitialPositionInStream){
		this.blockingQueueSize = blockingQueueSize;
		this.drInitialPositionInStream = drInitialPositionInStream;
	}

	@Override
	public void setDatarouterStreamSubscriberAccessor(DatarouterStreamSubscriberAccessor subscriberAccessor){
		this.subscriberAccessor = subscriberAccessor;
	}

	public DatarouterStreamSubscriberConfig withInitialPositionInStream(
			DrInitialPositionInStream drInitialPositionInStream){
		this.drInitialPositionInStream = drInitialPositionInStream;
		return this;
	}

	public DatarouterStreamSubscriberConfig withTimestampAtInitialPositionInStream(Date timestamp) {
		this.timestamp = timestamp;
		this.drInitialPositionInStream = DrInitialPositionInStream.AT_TIMESTAMP;
		return this;
	}

	public DatarouterStreamSubscriberConfig withExplicitSubscriberAppName(String subscriberAppName){
		this.subscriberAppName = subscriberAppName;
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

	public String getSubscriberAppName(){
		return subscriberAppName;
	}

	public DrInitialPositionInStream getDrInitialPositionInStream(){
		return drInitialPositionInStream;
	}

	public static enum DrInitialPositionInStream{
		LATEST,
		OLDEST,
		AT_TIMESTAMP
	}
}
