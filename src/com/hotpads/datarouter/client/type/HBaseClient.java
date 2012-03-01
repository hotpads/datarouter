package com.hotpads.datarouter.client.type;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.datastructs.MutableString;

public interface HBaseClient
extends Client{

	public static final Long DEFAULT_TIMEOUT_MS = 10 * 1000L;

	HTable checkOutHTable(String name, MutableString progress);
	void checkInHTable(HTable hTable, boolean possiblyTarnished);
	HTablePool getHTablePool();
	Integer getTotalPoolSize();
	ExecutorService getExecutorService();
	HBaseAdmin getHBaseAdmin();

	Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName);

	void shutdown();

}
