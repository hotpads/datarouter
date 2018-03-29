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
package io.datarouter.storage.util;

import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.counter.Counters;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public class DatarouterCounters{

	public static final String
		PREFIX = "Datarouter",
		CLIENT_TYPE_virtual = "virtual",
		AGGREGATION_op = "op",
		AGGREGATION_client = "client",
		AGGREGATION_table = "table",
		AGGREGATION_node = "node",
		AGGREGATION_region = "region",
		AGGREGATION_client_server = "client-server",
		AGGREGATION_client_server_table = "client-server-table",
		AGGREGATION_client_server_table_region = "client-server-table-region",
		AGGREGATION_client_server_table_op = "client-server-table-op",
		AGGREGATION_client_table_server = "client-table-server",
		AGGREGATION_client_table_server_op = "client-table-server-op";


	/*------------ node -------------------*/

	public static void incOp(ClientType type, String key){
		incInternal(AGGREGATION_op, type, key, 1L);
	}

	/*------------ node -------------------*/

	public static void incNode(String key, String nodeName, long delta){
		incInternal(AGGREGATION_op, null, key, delta);
		String compoundKey = nodeName + " " + key;
		incInternal(AGGREGATION_node, null, compoundKey, delta);
	}

	// allow node implementations to add whatever counts they want, prefixing them with "custom"
	public static void incClientNodeCustom(ClientType type, String key, String clientName, String nodeName, long delta){
		incClient(type, key, clientName, delta);
		String compoundKey = clientName + " " + nodeName + " custom " + key;
		incInternal(AGGREGATION_node, type, compoundKey, delta);
	}

	public static void incFromCounterAdapter(PhysicalNode<?,?,?> physicalNode, String key, long delta){
		ClientType clientType = physicalNode.getClient().getType();
		String clientName = physicalNode.getClient().getName();
		String nodeName = physicalNode.getName();
		incClient(clientType, key, clientName, delta);
		String compoundKey = clientName + " " + nodeName + " " + key;
		incInternal(AGGREGATION_node, clientType, compoundKey, delta);
	}

	/*------------ client -------------------*/

	public static void incClient(ClientType type, String key, String clientName, long delta){
		incInternal(AGGREGATION_op, type, key, delta);
		String compoundKey = clientName + " " + key;
		incInternal(AGGREGATION_client, type, compoundKey, delta);
	}
	/*------------ table -------------------*/

	public static void incClientTable(ClientType type, String key, String clientName, String tableName, long delta){
		incClient(type, key, clientName, delta);
		String compoundKey = clientName + " " + tableName + " " + key;
		incInternal(AGGREGATION_table, type, compoundKey, delta);
	}

	/*------------ region -------------------*/

	public static void incClientTableOpRegion(String clientTypeString, String clientName, String tableName,
			String opName, String regionName, long delta){
		String compoundKey = clientName + " " + tableName + " " + opName + " " + regionName;
		incInternalStringWithClientType(AGGREGATION_region, clientTypeString, compoundKey, delta);
	}

	/*------------ private -------------------*/

	private static void incInternal(String aggregationLevel, ClientType clientType, String key, long delta){
		String clientTypeString = clientType != null ? clientType.getName() : CLIENT_TYPE_virtual;
		incInternalStringWithClientType(aggregationLevel, clientTypeString, key, delta);
	}

	private static void incInternalStringWithClientType(String aggregationLevel, String clientTypeString, String key,
			long delta){
		Counters.inc(PREFIX + " " + aggregationLevel + " " + clientTypeString + " " + key, delta);
	}
}
