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

public class TableConfiguration{

	public final ClientTableEntityPrefixNameWrapper nodeNameWrapper;
	public final Long maxThreshold;
	public final Long sampleInterval;
	public final Integer batchSize;
	public final boolean isCountable;
	public final boolean enablePercentChangeAlert;
	public final boolean enableThresholdAlert;

	public TableConfiguration(ClientTableEntityPrefixNameWrapper nodeNameWrapper, Long maxThreshold,
			Long sampleInterval, Integer batchSize, boolean isCountable, boolean enablePercentChangeAlert,
			boolean enableThresholdAlert){
		this.nodeNameWrapper = nodeNameWrapper;
		this.maxThreshold = maxThreshold;
		this.sampleInterval = sampleInterval;
		this.batchSize = batchSize;
		this.isCountable = isCountable;
		this.enablePercentChangeAlert = enablePercentChangeAlert;
		this.enableThresholdAlert = enableThresholdAlert;
	}

	public TableConfiguration(String clientName, String tableName, String subEntityPrefix, Long maxThreshold,
			Long sampleInterval, Integer batchSize, boolean isCountable, boolean enablePercentChangeAlert,
			boolean enableThresholdAlert){
		this(new ClientTableEntityPrefixNameWrapper(clientName, tableName, subEntityPrefix), maxThreshold,
				sampleInterval, batchSize, isCountable, enablePercentChangeAlert, enableThresholdAlert);
	}

}
