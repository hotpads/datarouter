package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Comparator;

import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.ServerName;

public class DrServerInfo{
	
	private final ServerName serverName;
	private final HServerLoad hServerLoad;

	private final String name;
	private final String hostname;
	
	
	public DrServerInfo(ServerName serverName, HServerLoad hServerLoad){
		this.serverName = serverName;
		this.hServerLoad = hServerLoad;
		this.name = serverName.getServerName();
		this.hostname = serverName.getHostname();
	}
	
	
	/************** comparator ******************/
	
	public static class DrhServerInfoHigherLoadComparator implements Comparator<DrServerInfo>{
		@Override
		public int compare(DrServerInfo serverA, DrServerInfo serverB){
			int numARegions = serverA.gethServerLoad().getLoad();
			int numBRegions = serverB.gethServerLoad().getLoad();
			return numBRegions - numARegions;
		}
	}
	
	
	/************** get/set ***************************/

	public String getName(){
		return name;
	}
	
	public String getHostname(){
		return hostname;
	}

	public ServerName getServerName() {
		return serverName;
	}

	public HServerLoad gethServerLoad() {
		return hServerLoad;
	}

	
}