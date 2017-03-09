package com.hotpads.datarouter.util;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.profile.counter.Counters;

public class DRCounters{

	public static final String
		PREFIX = "Datarouter",
		CLIENT_TYPE_virtual = "virtual",
		AGGREGATION_op = "op",
		AGGREGATION_client = "client",
		AGGREGATION_table = "table",
		AGGREGATION_node = "node",
		AGGREGATION_region = "region",
		AGGREGATION_client_server_table = "client-server-table",
		AGGREGATION_client_server_table_op = "client-server-table-op",
		AGGREGATION_client_table_server = "client-table-server",
		AGGREGATION_client_table_server_op = "client-table-server-op";


	public static void incClient(ClientType type, String key, String clientName){
		incClient(type, key, clientName, 1L);
	}

	public static void incClientTable(ClientType type, String key, String clientName, String tableName){
		incClientTable(type, key, clientName, tableName, 1L);
	}

	// node name is usually of the format clientName.nodeName, so don't need separate client field
	public static void incClientNodeCustom(ClientType type, String key, String clientName, String nodeName){
		incClientNodeCustom(type, key, clientName, nodeName, 1L);
	}

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

	public static void incFromCounterAdapter(PhysicalNode<?,?> physicalNode, String key, long delta){
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

	/*------------ server -------------------*/

	public static void incServer(String clientName, String tableName, String opName, String serverName, long delta){
		incClientServerTable(clientName, tableName, serverName, delta);
		incClientServerTableOp(clientName, tableName, serverName, opName, delta);
		incClientTableServer(clientName, tableName, serverName, delta);
		incClientTableServerOp(clientName, tableName, serverName, opName, delta);
	}

	private static void incClientServerTable(String clientName, String tableName, String serverName, long delta){
		String compoundKey = clientName + " " + serverName + " " + tableName;
		incInternalString(AGGREGATION_client_server_table, compoundKey, delta);
	}

	private static void incClientServerTableOp(String clientName, String tableName, String serverName, String opName,
			long delta){
		String compoundKey = clientName + " " + serverName + " " + tableName + " " + opName;
		incInternalString(AGGREGATION_client_server_table_op, compoundKey, delta);
	}

	private static void incClientTableServer(String clientName, String tableName, String serverName, long delta){
		String compoundKey = clientName + " " + tableName + " " + serverName;
		incInternalString(AGGREGATION_client_table_server, compoundKey, delta);
	}

	private static void incClientTableServerOp(String clientName, String tableName, String serverName, String opName,
			long delta){
		String compoundKey = clientName + " " + tableName + " " + serverName + " " + opName;
		incInternalString(AGGREGATION_client_table_server_op, compoundKey, delta);
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

	private static void incInternalString(String aggregationLevel, String key, long delta){
		Counters.inc(PREFIX + " " + aggregationLevel + " " + key, delta);
	}
}
