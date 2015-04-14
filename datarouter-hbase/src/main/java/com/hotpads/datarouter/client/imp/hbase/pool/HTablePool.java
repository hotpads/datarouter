package com.hotpads.datarouter.client.imp.hbase.pool;

import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.util.datastructs.MutableString;

public interface HTablePool {

	HTable checkOut(String tableName, MutableString progress);
	void checkIn(HTable hTable, boolean possiblyTarnished);
	
	Integer getTotalPoolSize();
	
	void shutdown();
	
}