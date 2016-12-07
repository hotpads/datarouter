package com.hotpads.datarouter.client.imp.hbase.node.callback;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.client.coprocessor.Batch;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.util.DRCounters;

public class CountingBatchCallback<R> implements Batch.Callback<R>{

	private final DRCounters datarouterCounters;
	private final Client client;
	private final String tableName;
	private final String opName;


	public CountingBatchCallback(DRCounters datarouterCounters, Client client, String tableName, String opName){
		this.datarouterCounters = datarouterCounters;
		this.client = client;
		this.tableName = tableName;
		this.opName = opName;
	}


	@Override
	public void update(byte[] region, byte[] row, R result){
//		String regionString = Bytes.toString(region);
		String encodedRegionName = HRegionInfo.encodeRegionName(region);
		String opString = opName + " rows";
		datarouterCounters.incClientTableOpRegion(client, tableName, opString, encodedRegionName, 1);
	}

}
