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
package io.datarouter.storage.node.tableconfig;

public class TableConfigurationFactory{

	public static final Long DEFAULT_SAMPLE_INTERVAL = 1_000_000L;
	public static final Integer DEFAULT_BATCH_SIZE = 1000;

	private Long maxThreshold;
	private Long sampleInterval;
	private Integer batchSize;
	private boolean isCountable;
	private boolean enablePercentChangeAlert;
	private boolean enableThresholdAlert;

	public TableConfigurationFactory(){
		this.sampleInterval = DEFAULT_SAMPLE_INTERVAL;
		this.batchSize = DEFAULT_BATCH_SIZE;
		this.isCountable = true;
		this.enablePercentChangeAlert = true;
		this.enableThresholdAlert = true;
	}

	public TableConfigurationFactory setMaxThreshold(Long maxThreshold){
		this.maxThreshold = maxThreshold;
		return this;
	}

	public TableConfigurationFactory setSampleInterval(Long sampleInterval){
		this.sampleInterval = sampleInterval;
		return this;
	}

	public TableConfigurationFactory setBatchSize(Integer batchSize){
		this.batchSize = batchSize;
		return this;
	}

	public TableConfigurationFactory setCountable(boolean isCountable){
		this.isCountable = isCountable;
		return this;
	}

	public TableConfigurationFactory setEnablePercentChangeAlert(boolean enablePercentChangeAlert){
		this.enablePercentChangeAlert = enablePercentChangeAlert;
		return this;
	}

	public TableConfigurationFactory setEnableThresholdAlert(boolean enableThresholdAlert){
		this.enableThresholdAlert = enableThresholdAlert;
		return this;
	}

	public TableConfiguration create(ClientTableEntityPrefixNameWrapper wrapper){
		return new TableConfiguration(
				wrapper,
				maxThreshold,
				sampleInterval,
				batchSize,
				isCountable,
				enablePercentChangeAlert,
				enableThresholdAlert);
	}

}
