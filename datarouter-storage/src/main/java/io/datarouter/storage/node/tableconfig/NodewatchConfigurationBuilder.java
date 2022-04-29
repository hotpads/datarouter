/*
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

public class NodewatchConfigurationBuilder{

	public static final int DEFAULT_SAMPLE_SIZE = 1_000_000;
	public static final int DEFAULT_BATCH_SIZE = 1_000;

	private boolean enabled;
	private int sampleSize;
	private int batchSize;
	private boolean enabledPercentageAlert;
	private boolean enabledThresholdAlert;
	private Long threshold;

	public NodewatchConfigurationBuilder(){
		this.enabled = true;
		this.sampleSize = DEFAULT_SAMPLE_SIZE;
		this.batchSize = DEFAULT_BATCH_SIZE;
		this.enabledPercentageAlert = true;
		this.enabledThresholdAlert = true;
	}

	public NodewatchConfigurationBuilder withSampleSize(int sampleSize){
		this.sampleSize = sampleSize;
		return this;
	}

	public NodewatchConfigurationBuilder withBatchSize(int batchSize){
		this.batchSize = batchSize;
		return this;
	}

	public NodewatchConfigurationBuilder withThreshold(Long threshold){
		this.threshold = threshold;
		return this;
	}

	public NodewatchConfigurationBuilder disable(){
		this.enabled = false;
		return this;
	}

	public NodewatchConfigurationBuilder disablePercentChangeAlert(){
		this.enabledPercentageAlert = false;
		return this;
	}

	public NodewatchConfigurationBuilder disableMaxThresholdAlert(){
		this.enabledThresholdAlert = false;
		return this;
	}

	public NodewatchConfiguration create(ClientTableEntityPrefixNameWrapper wrapper){
		return new NodewatchConfiguration(
				wrapper,
				threshold,
				sampleSize,
				batchSize,
				enabled,
				enabledPercentageAlert,
				enabledThresholdAlert);
	}

}
