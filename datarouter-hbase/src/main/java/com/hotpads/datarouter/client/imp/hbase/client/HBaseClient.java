package com.hotpads.datarouter.client.imp.hbase.client;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Table;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.pool.HBaseTablePool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.datastructs.MutableString;

public interface HBaseClient
extends Client{

	Table checkOutTable(String name, MutableString progress);
	void checkInTable(Table table, boolean possiblyTarnished);
	HBaseTablePool getHTablePool();
	ExecutorService getExecutorService();
	Admin getAdmin();

	Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName);
}
