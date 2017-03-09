package com.hotpads.datarouter.client.imp.hbase.node.callback;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.util.DRCounters;

public class CountingBatchCallback<R> implements Batch.Callback<R>{
	private static final Logger logger = LoggerFactory.getLogger(CountingBatchCallback.class);

	private final HBaseNode<?,?,?> node;
	private final HBaseSubEntityNode<?,?,?,?,?> subEntityNode;
	private final String clientTypeString;
	private final String clientName;
	private final String tableName;
	private final String opName;


	public CountingBatchCallback(HBaseNode<?,?,?> node, String clientTypeString, String opName){
		this.node = node;
		this.subEntityNode = null;
		this.clientTypeString = clientTypeString;
		this.clientName = node.getClientTableNodeNames().getClientName();
		this.tableName = node.getClientTableNodeNames().getTableName();
		this.opName = opName;
	}

	public CountingBatchCallback(HBaseSubEntityNode<?,?,?,?,?> node, String clientTypeString,
			String opName){
		this.node = null;
		this.subEntityNode = node;
		this.clientTypeString = clientTypeString;
		this.clientName = node.getClientTableNodeNames().getClientName();
		this.tableName = node.getClientTableNodeNames().getTableName();
		this.opName = opName;
	}


	@Override
	public void update(byte[] region, byte[] row, R result){
		String encodedRegionName = HRegionInfo.encodeRegionName(region);
		String opString = opName + " rows";
		DRCounters.incClientTableOpRegion(clientTypeString, clientName, tableName, opString, encodedRegionName, 1);
		try{
			RegionLocator regionLocator = getClient().getConnection().getRegionLocator(TableName.valueOf(tableName));
			HRegionLocation regionLocation = regionLocator.getRegionLocation(row);
			ServerName serverName = regionLocation.getServerName();
			String hostname = serverName.getHostname();//could add port and serverStartCode in the future
			logger.debug("{}, {}, {}", tableName, hostname, opName);
			DRCounters.incServer(clientName, tableName, opName, hostname, 1L);
		}catch(Exception e){
			logger.warn("", e);
		}
	}

	//don't cache the client since it could change
	private HBaseClient getClient(){
		return node != null ? node.getClient() : subEntityNode.getClient();
	}

}
