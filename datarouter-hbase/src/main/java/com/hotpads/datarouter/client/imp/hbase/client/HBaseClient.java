package com.hotpads.datarouter.client.imp.hbase.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Table;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.datastructs.MutableString;

public interface HBaseClient
extends Client{

	Table checkOutTable(String name, MutableString progress) throws IOException;
	void checkInTable(Table table, boolean possiblyTarnished) throws IOException;
	ExecutorService getExecutorService();
	@Deprecated
	HBaseAdmin getHBaseAdmin();
	Admin getAdmin();

	Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName);
}
