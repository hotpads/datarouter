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
package io.datarouter.client.hbase.callback;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.node.nonentity.HBaseNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityNode;
import io.datarouter.client.hbase.util.DatarouterHBaseCounters;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.util.collection.CollectionTool;

@Singleton
public class CountingBatchCallbackFactory{
	private static final Logger logger = LoggerFactory.getLogger(CountingBatchCallback.class);

	@Inject
	private HBaseClientManager hBaseClientManager;

	public class CountingBatchCallback<R> implements Batch.Callback<R>{

		private final ClientId clientId;
		private final String clientName;
		private final String tableName;
		private final String opName;

		public CountingBatchCallback(HBaseNode<?,?,?,?,?> node, String opName){
			this(CollectionTool.getFirst(node.getClientIds()), node.getClientTableNodeNames(), opName);
		}

		public CountingBatchCallback(HBaseSubEntityNode<?,?,?,?,?> node, String opName){
			this(CollectionTool.getFirst(node.getClientIds()), node.getClientTableNodeNames(), opName);
		}

		public CountingBatchCallback(ClientId clientId, ClientTableNodeNames clientTableNodeNames, String opName){
			this.clientId = clientId;
			this.clientName = clientTableNodeNames.getClientName();
			this.tableName = clientTableNodeNames.getTableName();
			this.opName = opName;
		}

		@Override
		public void update(byte[] region, byte[] row, R result){
			try{
				String regionName = HRegionInfo.encodeRegionName(region);
				Connection connection = hBaseClientManager.getConnection(clientId);
				RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf(tableName));
				HRegionLocation regionLocation = regionLocator.getRegionLocation(row);
				ServerName serverName = regionLocation.getServerName();
				String hostname = serverName.getHostname();// could add port and serverStartCode in the future
				logger.debug("{}, {}, {}, {}, {}", clientName, tableName, opName, regionName, hostname);
				DatarouterHBaseCounters.onHBaseRowCallback(clientName, tableName, opName, regionName, hostname, 1L);
				TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
						.add("regionName", regionName)
						.add("hostname", hostname));
			}catch(Exception e){
				logger.warn("", e);
			}
		}

	}

}