package com.hotpads.datarouter.client.type;

import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.Client;

public interface HBaseClient
extends Client{
	
	HTable checkOutHTable(byte[] name);
	void checkInHTable(HTable hTable);
	
}
