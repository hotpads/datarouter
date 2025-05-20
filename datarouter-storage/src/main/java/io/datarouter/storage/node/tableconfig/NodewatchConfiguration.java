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

public class NodewatchConfiguration{

	public final ClientTableEntityPrefixNameWrapper nodeNameWrapper;
	public final Long maxThreshold;
	public final int sampleSize;
	public final int batchSize;
	public final boolean isCountable;
	public final boolean enablePercentageAlert;
	public final boolean enableThresholdAlert;
	// Whether to export periodically to Blockfile
	public final boolean enableShadowTableExport;
	public final boolean enableShadowTableCompression;
	public final int shadowTableScanBatchSize;

	public NodewatchConfiguration(
			ClientTableEntityPrefixNameWrapper nodeNameWrapper,
			Long maxThreshold,
			int sampleSize,
			int batchSize,
			boolean isCountable,
			boolean enablePercentageAlert,
			boolean enableThresholdAlert,
			boolean enableShadowTableExport,
			boolean enableShadowTableCompression,
			int shadowTableScanBatchSize){
		this.nodeNameWrapper = nodeNameWrapper;
		this.maxThreshold = maxThreshold;
		this.sampleSize = sampleSize;
		this.batchSize = batchSize;
		this.isCountable = isCountable;
		this.enablePercentageAlert = enablePercentageAlert;
		this.enableThresholdAlert = enableThresholdAlert;
		this.enableShadowTableExport = enableShadowTableExport;
		this.enableShadowTableCompression = enableShadowTableCompression;
		this.shadowTableScanBatchSize = shadowTableScanBatchSize;
	}

	public NodewatchConfiguration(
			String clientName,
			String tableName,
			String subEntityPrefix,
			Long maxThreshold,
			int sampleSize,
			int batchSize,
			boolean isCountable,
			boolean enablePercentageAlert,
			boolean enableThresholdAlert,
			boolean enableShadowTableExport,
			boolean enableShadowTableCompression,
			int shadowTableScanBatchSize){
		this(
				new ClientTableEntityPrefixNameWrapper(clientName, tableName, subEntityPrefix),
				maxThreshold,
				sampleSize,
				batchSize,
				isCountable,
				enablePercentageAlert,
				enableThresholdAlert,
				enableShadowTableExport,
				enableShadowTableCompression,
				shadowTableScanBatchSize);
	}

}
