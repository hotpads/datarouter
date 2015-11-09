package com.hotpads.datarouter.client.imp.hbase.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Table;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface HBaseClient
extends Client{

	Table checkOutTable(String name) throws IOException;
	void checkInTable(Table table) throws IOException;
	ExecutorService getExecutorService();
	@Deprecated
	HBaseAdmin getHBaseAdmin();
	Admin getAdmin();

	Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName);
}
