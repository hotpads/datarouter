package com.hotpads.datarouter.client.type;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.Client;

public interface HBaseClient
extends Client{
	
	HTable checkOutHTable(String name);
	void checkInHTable(HTable hTable);
	
	ExecutorService getExecutorService();
	
}
