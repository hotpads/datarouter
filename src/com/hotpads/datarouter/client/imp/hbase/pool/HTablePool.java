package com.hotpads.datarouter.client.imp.hbase.pool;

import org.apache.hadoop.hbase.client.HTable;

public interface HTablePool {

	HTable checkOut(String tableName);
	void checkIn(HTable hTable, boolean possiblyTarnished);
	
	Integer getTotalPoolSize();
	
}