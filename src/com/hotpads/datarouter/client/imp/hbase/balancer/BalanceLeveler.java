package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

public class BalanceLeveler{

	private SortedMap<DRHRegionInfo<?>,ServerName> originalServerByRegion;
	private SortedMap<DRHRegionInfo<?>,ServerName> workingServerByRegion;
	
	private long minForServer;
	private long maxForServer;
	private Map<ServerName,Long> workingCountByServer;
	
	
	/************** construct ***************************/
	
	public BalanceLeveler(SortedMap<DRHRegionInfo<?>,ServerName> serverByRegion){
		this.originalServerByRegion = serverByRegion;
		this.workingServerByRegion = new TreeMap<DRHRegionInfo<?>,ServerName>(serverByRegion);
		updateWorkingCountByServer();
	}
	
	
	/************* public methods ***********************/
	
	public SortedMap<DRHRegionInfo<?>,ServerName> getBalancedServerByRegion(){
		while( ! isBalanced()){
			ServerName mostLoadedServer = getMostLoadedServer();
			DRHRegionInfo<?> firstRegionOnServer = getFirstRegionOnServer(mostLoadedServer);
			ServerName leastLoadedServer = getLeastLoadedServer();
			//overwrite the region's serverName, thus moving it
			workingServerByRegion.put(firstRegionOnServer, leastLoadedServer);
			updateWorkingCountByServer();
		}
		return workingServerByRegion;
	}
	
	
	/*************** private methods **************************/
	
	private void updateWorkingCountByServer(){
		for(Map.Entry<DRHRegionInfo<?>,ServerName> entry : originalServerByRegion.entrySet()){
			MapTool.increment(workingCountByServer, entry.getValue());
		}
		this.minForServer = ComparableTool.getFirst(workingCountByServer.values());
		this.maxForServer = ComparableTool.getLast(workingCountByServer.values());
	}
	
	private boolean isBalanced(){
		return maxForServer - minForServer <= 1;
	}
	
	private ServerName getMostLoadedServer(){
		for(Map.Entry<ServerName,Long> entry : workingCountByServer.entrySet()){
			if(entry.getValue() == maxForServer){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("max values out of sync");
	}
	
	private ServerName getLeastLoadedServer(){
		for(Map.Entry<ServerName,Long> entry : workingCountByServer.entrySet()){
			if(entry.getValue() == minForServer){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("min values out of sync");
	}
	
	
	private DRHRegionInfo<?> getFirstRegionOnServer(ServerName serverName){
		for(Map.Entry<DRHRegionInfo<?>,ServerName> entry : workingServerByRegion.entrySet()){
			if(ObjectTool.equals(serverName, entry.getValue())){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("value didn't exist in map");
	}
}
