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
		AGGREGATION_node = "node";

	
	/********* inc single ************/
	
	public static void incOp(ClientType type, String key) {
		incInternal(AGGREGATION_op, type, key, 1L);
	}
	
	public static void incClient(ClientType type, String key, String clientName) {
		incClient(type, key, clientName, 1L);
	}
	
	public static void incClientTable(ClientType type, String key, String clientName, String tableName) {
		incClientTable(type, key, clientName, tableName, 1L);
	}
	
//	public static void incFromCounterAdapter(ClientType type, String key, String clientName, String nodeName) {
//		incClientNodeCustom(type, key, clientName, nodeName, 1L);
//	}
	
	//node name is usually of the format clientName.nodeName, so don't need separate client field
	public static void incClientNodeCustom(ClientType type, String key, String clientName, String nodeName) {
		incClientNodeCustom(type, key, clientName, nodeName, 1L);
	}
	
	
	/******** inc multi *************/
	
	public static void incNode(String key, String nodeName, long delta){
		incInternal(AGGREGATION_op, null, key, delta);
		String compoundKey = nodeName+" "+key;
		incInternal(AGGREGATION_node, null, compoundKey, delta);
	}
	
	public static void incClient(ClientType type, String key, String clientName, long delta){
		incInternal(AGGREGATION_op, type, key, delta);
		String compoundKey = clientName+" "+key;
		incInternal(AGGREGATION_client, type, compoundKey, delta);
	}
	
	public static void incClientTable(ClientType type, String key, String clientName, String tableName, 
			long delta){
		incClient(type, key, clientName, delta);
		String compoundKey = clientName+" "+tableName+" "+key;
		incInternal(AGGREGATION_table, type, compoundKey, delta);
	}
	
	//allow node implementations to add whatever counts they want, prefixing them with "custom"
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
		String compoundKey = clientName+" "+nodeName+" "+key;
		incInternal(AGGREGATION_node, clientType, compoundKey, delta);
	}

	
	/********* private ***********/

	private static void incInternal(String aggregationLevel, ClientType clientType, String key, long delta) {
		String clientTypeString = clientType != null ? clientType.getName() : CLIENT_TYPE_virtual;
		Counters.inc(PREFIX+" "+aggregationLevel+" "+clientTypeString+" "+key, delta);
	}
}
