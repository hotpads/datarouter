package com.hotpads.datarouter.client.imp.hbase.pool;

import org.apache.hadoop.hbase.client.Table;

import com.hotpads.util.datastructs.MutableString;

public interface HBaseTablePool {

	Table checkOut(String tableName, MutableString progress);
	void checkIn(Table table, boolean possiblyTarnished);

	Integer getTotalPoolSize();

	void shutdown();

}