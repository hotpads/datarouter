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
package io.datarouter.client.hbase.util;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.util.DatarouterCounters;

public class DatarouterHBaseCounters{

	public static void onHBaseRowCallback(String clientName, String tableName, String opName, String regionName,
			String serverName, long delta){
		onHBaseRowCallbackClientServer(clientName, serverName, delta);
		onHBaseRowCallbackClientServerTable(clientName, tableName, serverName, delta);
		onHBaseRowCallbackClientServerTableRegion(clientName, tableName, serverName, regionName, delta);
		onHBaseRowCallbackClientServerTableOp(clientName, tableName, serverName, opName, delta);
		onHBaseRowCallbackClientTableServer(clientName, tableName, serverName, delta);
		onHBaseRowCallbackClientTableServerOp(clientName, tableName, serverName, opName, delta);
	}

	private static void onHBaseRowCallbackClientServer(String clientName, String serverName, long delta){
		String key = clientName + " " + serverName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_server, key, delta);
	}

	private static void onHBaseRowCallbackClientServerTable(String clientName, String tableName, String serverName,
			long delta){
		String key = clientName + " " + serverName + " " + tableName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_server_table, key, delta);
	}

	private static void onHBaseRowCallbackClientServerTableRegion(String clientName, String tableName,
			String serverName, String regionName, long delta){
		String key = clientName + " " + serverName + " " + tableName + " " + regionName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_server_table_region, key, delta);
	}

	private static void onHBaseRowCallbackClientServerTableOp(String clientName, String tableName, String serverName,
			String opName, long delta){
		String key = clientName + " " + serverName + " " + tableName + " " + opName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_server_table_op, key, delta);
	}

	private static void onHBaseRowCallbackClientTableServer(String clientName, String tableName, String serverName,
			long delta){
		String key = clientName + " " + tableName + " " + serverName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_table_server, key, delta);
	}

	private static void onHBaseRowCallbackClientTableServerOp(String clientName, String tableName, String serverName,
			String opName, long delta){
		String key = clientName + " " + tableName + " " + serverName + " " + opName;
		onHBaseRowCallbackInternal(DatarouterCounters.AGGREGATION_client_table_server_op, key, delta);
	}

	private static void onHBaseRowCallbackInternal(String aggregationLevel, String key, long delta){
		Counters.inc(DatarouterCounters.PREFIX + " " + aggregationLevel + " rows " + key, delta);
	}

}
