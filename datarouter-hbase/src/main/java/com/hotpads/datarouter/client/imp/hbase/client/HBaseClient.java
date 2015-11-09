package com.hotpads.datarouter.client.imp.hbase.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.datastructs.MutableString;

public interface HBaseClient
extends Client{

	@Deprecated
	HTable checkOutHTable(String name, MutableString progress);
	Table checkOutTable(String name) throws IOException;
	@Deprecated
	void checkInHTable(HTable htable, boolean possiblyTarnished);
	void checkInTable(Table table) throws IOException;
	HTablePool getHTablePool();
	Integer getTotalPoolSize();
	ExecutorService getExecutorService();
	@Deprecated
	HBaseAdmin getHBaseAdmin();
	Admin getAdmin();

	Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName);
}
