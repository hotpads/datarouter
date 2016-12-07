package com.hotpads.datarouter.client.imp.hbase.node.callback;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.client.coprocessor.Batch;

import com.hotpads.datarouter.util.DRCounters;

public class CountingBatchCallback<R> implements Batch.Callback<R>{

	private final DRCounters datarouterCounters;
	private final String clientTypeString;
	private final String clientName;
	private final String tableName;
	private final String opName;


	public CountingBatchCallback(DRCounters datarouterCounters, String clientTypeString, String clientName,
			String tableName, String opName){
		this.datarouterCounters = datarouterCounters;
		this.clientTypeString = clientTypeString;
		this.clientName = clientName;
		this.tableName = tableName;
		this.opName = opName;
	}


	@Override
	public void update(byte[] region, byte[] row, R result){
		String encodedRegionName = HRegionInfo.encodeRegionName(region);
		String opString = opName + " rows";
		datarouterCounters.incClientTableOpRegion(clientTypeString, clientName, tableName, opString, encodedRegionName,
				1);
	}

}
