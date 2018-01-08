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
package io.datarouter.storage.config.stream;

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
	private boolean replayData = false;

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

	public DatarouterStreamSubscriberConfig withTimestampAtInitialPositionInStreamAtTimestamp(Date timestamp){
		this.timestamp = timestamp;
		this.drInitialPositionInStream = DrInitialPositionInStream.AT_TIMESTAMP;
		return this;
	}

	public DatarouterStreamSubscriberConfig withExplicitSubscriberAppName(String subscriberAppName){
		this.subscriberAppName = subscriberAppName;
		return this;
	}

	public DatarouterStreamSubscriberConfig withReplayData(boolean replayData){
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

	public boolean getReplayData(){
		return replayData;
	}

	public Integer getInitialLeaseTableReadCapacity(){
		return initialLeaseTableReadCapacity;
	}

	public Integer getInitialLeaseTableWriteCapacity(){
		return initialLeaseTableWriteCapacity;
	}
}
