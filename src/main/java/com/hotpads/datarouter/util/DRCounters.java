package com.hotpads.datarouter.util;

import com.hotpads.datarouter.client.DClientType;
import com.hotpads.profile.count.collection.Counters;

public class DRCounters{
	
	public static final String
		PREFIX = "DataRouter",
		
		CLIENT_TYPE_virtual = "virtual",
		
		AGGREGATION_op = "op",
		AGGREGATION_client = "client",
		AGGREGATION_table = "table",
		AGGREGATION_node = "node";

	
	/********* inc single ************/
	
	public static Long incSuffixOp(DClientType type, String key) {
		return incInternal(AGGREGATION_op, type, key, 1L);
	}
	
	public static void incSuffixClient(DClientType type, String key, String clientName) {
		incSuffixClient(type, key, clientName, 1L);
	}
	
	public static void incSuffixClientTable(DClientType type, String key, String clientName, String tableName) {
		incSuffixClientTable(type, key, clientName, tableName, 1L);
	}
	
	//node name is usually of the format clientName.nodeName, so don't need separate client field
	public static void incSuffixClientNode(DClientType type, String key, String clientName, String nodeName) {
		incSuffixClientNode(type, key, clientName, nodeName, 1L);
	}
	
	
	/******** inc multi *************/
	
	public static void incSuffixNode(String key, String nodeName, long delta) {
		incInternal(AGGREGATION_op, null, key, delta);
		String compoundKey = nodeName+" "+key;
		incInternal(AGGREGATION_node, null, compoundKey, delta);
	}
	
	public static void incSuffixClient(DClientType type, String key, String clientName, long delta) {
		incInternal(AGGREGATION_op, type, key, delta);
		String compoundKey = clientName+" "+key;
		incInternal(AGGREGATION_client, type, compoundKey, delta);
	}
	
	public static void incSuffixClientTable(DClientType type, String key, String clientName, String tableName, long delta) {
		incSuffixClient(type, key, clientName, delta);
		String compoundKey = clientName+" "+tableName+" "+key;
		incInternal(AGGREGATION_table, type, compoundKey, delta);
	}
	
	public static void incSuffixClientNode(DClientType type, String key, String clientName, String nodeName, long delta) {
		incSuffixClient(type, key, clientName, delta);
		String compoundKey = clientName+" "+nodeName+" "+key;
		incInternal(AGGREGATION_node, type, compoundKey, delta);
	}

	
	/********* private ***********/

	private static Long incInternal(String aggregationLevel, DClientType clientType, String key, long delta) {
		String clientTypeString = clientType != null ? clientType.getName() : CLIENT_TYPE_virtual;
		return Counters.inc(PREFIX+" "+aggregationLevel+" "+clientType+" "+key, delta);
	}
}
