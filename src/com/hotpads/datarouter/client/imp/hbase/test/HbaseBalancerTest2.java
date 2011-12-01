package com.hotpads.datarouter.client.imp.hbase.test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import com.hotpads.util.core.MapTool;

@Deprecated//example code for https://issues.apache.org/jira/browse/HBASE-3373
public class HbaseBalancerTest2{

	public static final String ZK_QUORUM = "localhost";
	public static final Integer BUCKETS_PER_NODE = 50;
	
	@Test public void testBalance() throws IOException{
		Configuration config = HBaseConfiguration.create();
		config.set(HConstants.ZOOKEEPER_QUORUM, ZK_QUORUM);
		HBaseAdmin admin = new HBaseAdmin(config);
		ClusterStatus clusterStatus = admin.getClusterStatus();
		Collection<ServerName> hServers = clusterStatus.getServerInfo();
		
		//build the server hash ring
		SortedMap<Long,ServerName> consistentHashRing = MapTool.createTreeMap();
		for(ServerName serverName : hServers){
			for(int i = 0; i < BUCKETS_PER_NODE; ++i){
				String serverNamePlusBucketNumber = serverName.getHostAndPort()+i;
				Long bucketPosition = longDJBHash(serverNamePlusBucketNumber.getBytes());
				consistentHashRing.put(bucketPosition, serverName);
			}
		}
		
		//test to see if each region in each table is on the right server
		HTableDescriptor[] tables = admin.listTables();
		for(HTableDescriptor tableDescriptor : tables){
			System.out.println("table:"+tableDescriptor.getNameAsString());
			HTable table = new HTable(config, tableDescriptor.getName());
			Map<HRegionInfo,ServerName> hServerAddressByHRegionInfo = table.getRegionLocations();
			for(HRegionInfo regionInfo : hServerAddressByHRegionInfo.keySet()){
				System.out.println("\tregion "+regionInfo.getRegionNameAsString());
				Long regionHash = longDJBHash(regionInfo.getStartKey());
				if(!consistentHashRing.containsKey(regionHash)){
					SortedMap<Long,ServerName> tail = consistentHashRing.tailMap(regionHash);
					regionHash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
				}
				ServerName targetServer = consistentHashRing.get(regionHash);
				
				//trigger move to correct server if necessary
				ServerName currentServer = hServerAddressByHRegionInfo.get(regionInfo);
				if(!currentServer.equals(targetServer)){
					admin.move(Bytes.toBytes(regionInfo.getEncodedName()), 
							Bytes.toBytes(targetServer.getServerName()));
					System.out.println("\t\tmoving:"+regionInfo.getRegionNameAsString()+" to "
							+targetServer.getHostAndPort());
				}
			}
		}
	}
	
	
	public static long longDJBHash(byte[] bytes){
		long hash = 5381l;
		for(int i = 0; i < bytes.length; i++){
			hash = ((hash << 5) + hash) + bytes[i];
		}
		return (hash & 0x7FFFFFFFFFFFFFFFl);
	}
	
}
